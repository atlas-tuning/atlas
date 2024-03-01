package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.DataIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 *
 * This class is a utility to greatly accelerate reverse engineering of the DIT ECU by mapping DIDs to memory
 * locations. You can use the generated symbols in Ghidra/IDA/etc. to help identify functions that calculate
 * things that you care about, like ignition timing, AFR, and so on. From there, you may even be able to find
 * what table data was used to calculate these values and more easily name-out each table based on the memory
 * it is setting.
 *
 * The way this works is by leveraging the structure of the memory in the decrypted flash for a given Renesas-
 * based ECU (typically found in the Subaru DIT platform cars). They have several long arrays of different
 * DID ranges (i.e. 0x0100 to 0x01FF) where each entry describes a DID, a corresponding memory address to
 * use for that DID, and a size for the memory to read from that location (not exactly in that order, FYI;
 * see SubaruDITMemoryMapping for the exact ordering).
 *
 * Using this data, we can then cross-reference the DID declared in the flash memory with the DID (now called
 * "SID") in the corresponding "DTM.xml" file found in SSM4 (you must decrypt this; see Crypto.java). For
 * example, I am planning to use the 12002_DTM.xml file for the SUBARU_2022MY_USDM_WRX_MT pairing. Then, we
 * can rapidly automate the generation of symbols in our favorite reverse engineering tools to further
 * investigate table data/offsets.
 *
 * Keep in mind these offsets do indeed anchor at the true root of 0x0000000, which includes the bootloader
 * that is often not re-flashed.
 *
 * Have fun!
 */
public class SubaruDITMemoryMapper {

    // See: offset 0x0031b042 in memory (AKA code flash)
    // this is uds handle read data by identifier for standard/non-dynamic/non-periodic DIDs
    // called by 0x0031b252 AKA 0x22 UDS SID entrypoint
    private static List<SubaruDITMemoryLookupTable> SUBARU_2022MY_USDM_WRX_MT = Arrays.asList(
            new SubaruDITMemoryLookupTable(0x0100, 0x01FF, 0x0014c5c8, 0x41),
            new SubaruDITMemoryLookupTable(0x0200, 0x02FF, 0x0014c8d4, 0x1F),
            new SubaruDITMemoryLookupTable(0x1000, 0x10FF, 0x0014ca48, 0x59),
            new SubaruDITMemoryLookupTable(0x1100, 0x11FF, 0x0014ce74, 0x44),
            new SubaruDITMemoryLookupTable(0x1200, 0x12FF, 0x0014d1a4, 0x5c),
            new SubaruDITMemoryLookupTable(0x1300, 0x13FF, 0x0014d5f4, 0x74),
            new SubaruDITMemoryLookupTable(0x1400, 0x14FF, 0x0014db64, 0x0c),
            new SubaruDITMemoryLookupTable(0x2000, 0x20FF, 0x0014dbf4, 0x0d),
            new SubaruDITMemoryLookupTable(0x3000, 0x30FF, 0x0014dc90, 0x0c),
            new SubaruDITMemoryLookupTable(0x7000, 0x70FF, 0x0014e014, 0x36),
            new SubaruDITMemoryLookupTable(0x7400, 0x74FF, 0x0014c46c, 0x02),
            new SubaruDITMemoryLookupTable(0x7500, 0x75FF, 0x0014e29c, 200),
            new SubaruDITMemoryLookupTable(0x7600, 0x76FF, 0x0014ebfc, 0x9e),
            new SubaruDITMemoryLookupTable(0x7700, 0x77FF, 0x0014f364, 0x76),
            new SubaruDITMemoryLookupTable(0xF100, 0xF1FF, 0x0014dd20, 0x06),
            new SubaruDITMemoryLookupTable(0xf400, 0xf4FF, 0x0014dd68, 0x39)
    );

    public static void main(String[] args) throws Exception {
        File code_flash_file = new File(args[0]);
        RandomAccessFile raf = new RandomAccessFile(code_flash_file, "rw");

        File dtm_xml_file = new File(args[1]);

        File output_file = new File(args[2]);

        List<SubaruDITMemoryMapping> mappings = new ArrayList<>();
        for (SubaruDITMemoryLookupTable lut : SUBARU_2022MY_USDM_WRX_MT) {
            mappings.addAll(lut.readMappings(raf));
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(dtm_xml_file);

        Element dataMonitorTable = document.getDocumentElement();
        NodeList pids = dataMonitorTable.getElementsByTagName("Pid");
        Function<Integer, Element> lookupFunction = (did) -> {
            String didString = Integer.toHexString(did);

            // Left-side padding
            while (didString.length() < 4) {
                didString = "0" + didString;
            }

            for (int index = 0; index < pids.getLength(); index ++) {
                Element element = (Element) pids.item(index);
                if (element.getAttribute("PidNo").equalsIgnoreCase("$" + didString)) {
                    return element;
                }
            }

            throw new IllegalArgumentException("DID not found in DTM.xml: " + didString);
        };

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output_file))) {
            // Now, pair these mappings with the DTM.xml file
            for (SubaruDITMemoryMapping mapping : mappings) {
                String didString = Integer.toHexString(mapping.getDataIdentifierCode());

                // Left-side padding
                while (didString.length() < 4) {
                    didString = "0" + didString;
                }

                String signalName;
                try {
                    Element found = lookupFunction.apply(mapping.getDataIdentifierCode());
                    signalName = found.getAttribute("SignalName");
                } catch (IllegalArgumentException unknown) {
                    DataIdentifier identifier = mapping.getDataIdentifier();
                    try {
                        identifier.collapse();
                        signalName = identifier.name();
                    } catch (IllegalArgumentException ex) {
                        signalName = "UNKNOWN";
                    }
                }

                // More detail
                System.out.println(mapping + " symbol=DID_" + didString.toUpperCase() + "_" + signalName);

                // Ghidra
                writer.write("DID_" + didString.toUpperCase() + "_" + signalName
                        + " 0x" + Integer.toHexString(mapping.memory_address));
                writer.write('\n');
            }
        }
    }

    static class SubaruDITMemoryLookupTable {
        private final int did_range_start;
        private final int did_range_end;
        private final int offset;
        private final int number_mappings;

        SubaruDITMemoryLookupTable(int didRangeStart, int didRangeEnd, int offset, int numberMappings) {
            did_range_start = didRangeStart & 0xFFFF;
            did_range_end = didRangeEnd & 0xFFFF;
            this.offset = offset & 0xFFFFFFFF;
            number_mappings = numberMappings & 0xFF;
        }

        public int getOffset() {
            return offset;
        }

        public int getNumberMaappings() {
            return number_mappings;
        }

        public int getDidRangeStart() {
            return did_range_start;
        }

        public int getDidRangeEnd() {
            return did_range_end;
        }

        public List<SubaruDITMemoryMapping> readMappings(RandomAccessFile raf) throws IOException {
            raf.seek(offset);
            List<SubaruDITMemoryMapping> mappings = new ArrayList<>();
            for (int i = 0; i < getNumberMaappings(); i ++) {
                long offset = raf.getFilePointer();
                SubaruDITMemoryMapping mapping = SubaruDITMemoryMapping.read(raf);
                if (mapping.data_identifier < getDidRangeStart() || mapping.data_identifier > getDidRangeEnd()) {
                    throw new IllegalArgumentException("Unexpected DID:" +
                            " lut_address=" + Long.toHexString(offset) +
                            " index=" + i +
                            " address=" + Long.toHexString( - 12) +
                            " did_start=" + Integer.toHexString(did_range_start) +
                            " did_end=" + Integer.toHexString(did_range_end) +
                            " this_did=" + Integer.toHexString(mapping.data_identifier));
                }
                mappings.add(mapping);
            }
            return mappings;
        }
    }

    static class SubaruDITMemoryMapping {
        private final int offset;
        private final int data_identifier;
        private final int data_size;
        private final int memory_address;
        private final boolean read_inverted;
        private final byte[] unknown_maybe_scaling;

        SubaruDITMemoryMapping(int offset,
                               int dataIdentifier,
                               int dataSize,
                               int memoryAddress,
                               boolean read_inverted,
                               byte[] unknownMaybeScaling) {
            this.offset = offset;
            data_identifier = dataIdentifier;
            data_size = dataSize;
            memory_address = memoryAddress;
            this.read_inverted = read_inverted;
            unknown_maybe_scaling = unknownMaybeScaling;
        }

        public static SubaruDITMemoryMapping read(RandomAccessFile raf) throws IOException {
            int offset = (int) (raf.getFilePointer());
            byte[] chunk = new byte[12];
            raf.read(chunk);
            BitReader bitReader = new BitReader(new ByteArrayInputStream(chunk));
            return read(offset, bitReader);
        }

        public static SubaruDITMemoryMapping read(int offset, BitReader reader) throws IOException {
            // DID (See: DataIdentifier.java)
            int data_identifier = reader.readShort() & 0xFFFF;

            // LE -> BE conversion (RH850 stuff).
            data_identifier = ((((data_identifier >> 8) & 0xFF) | ((data_identifier << 8) & 0xFF00)) & 0xFFFF);

            // Amount of data that we read when we want to read the value of this DID.
            int data_size = reader.readByte() & 0xFF;

            reader.readByte();

            // Memory address to read from when we want to read the value of this DID.
            int memory_address = reader.readInt();

            // LE -> BE conversion (RH850 stuff).
            memory_address = Integer.reverseBytes(memory_address);

            byte directionFlag = reader.readByte();
            boolean backwards = directionFlag == 0xFF;

            // Not sure what these are yet. Probably scaling data/etc. We don't care, though.
            byte[] unknown_maybe_scaling = reader.readBytes(3);

            return new SubaruDITMemoryMapping(
                    offset,
                    data_identifier,
                    data_size, memory_address, backwards,
                    unknown_maybe_scaling
            );
        }

        public DataIdentifier getDataIdentifier() {
            return DataIdentifier.findByDid((short) (getDataIdentifierCode() & 0xFFFF));
        }

        public int getDataIdentifierCode() {
            return data_identifier;
        }

        public int getDataSize() {
            return data_size;
        }

        public int getMemoryAddress() {
            return memory_address;
        }

        @Override
        public String toString() {
            return "offset=" + Integer.toHexString(offset) +
                    " did=" + Integer.toHexString(data_identifier) +
                    " address=" + Integer.toHexString(memory_address) +
                    " sz=" + data_size;
        }
    }

}
