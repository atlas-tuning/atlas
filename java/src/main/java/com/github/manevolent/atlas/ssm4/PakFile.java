package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.windows.CryptoAPI;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility reads Subaru FlashWrite ".pak" or ".pk2" files, which belong to the Subaru FlashWrite
 * utility (versions 1 and 2)
 *
 * The PAK format is specific to the Denso software package (FlashWrite); they're not standardized.
 *
 * PAK files are MFC CArchive objects, meaning they're directly serialized from C++ objects using
 * the older MFC framework.  Each entry is protected with a 40-bit key, 64-bit block RC2 cipher
 * which has been reverse engineered from the Microsoft CryptoAPI Base Cryptography Provider in
 * the CryptoAPI.java sister file.
 *
 * This utility has a main method.  Point it at a Subaru-supplied CSV, and point at a directory
 * containing PAK files (i.e. the "EcuData" folder shipped with SSM3/4).  This utility will
 * auto-magically unpack each of the entries in the PAK file (there can be several) and save them
 * to your disk using the "keyword" supplied in each file.
 *
 * Keep in mind that there is more reverse engineering needed with these PAK files, since each file
 * contains multiple sub-files, incl. filenames.  Think of them like encrypted archives.
 *
 * NOTE ON COPYRIGHT:
 * PAK files and the CSV themselves are not supplied as they are proprietary.  You need a copy
 * of SSM3/SSM4 to run this utility and inspect the output(s).
 *
 * Example arg array:
 * [0]: ".../flashwrite/Pack File Database_N_AMERICA.csv"
 * [1] ".../flashwrite/EcuData/"
 *
 * ..outputs files like this:
 * .../flashwrite/EcuData/FILENAME.pak.KEYWORD/(EcuDataMap,PcVerData,flash binaries,etc.)
 */
public class PakFile {
    private static final byte[] anchor = "CClFileDataInfo".getBytes(StandardCharsets.US_ASCII);

    public static void main(String[] args) throws Exception {
        decrypt(args[0], args[1]);
    }

    private static void decrypt(String csvFile, String pakDirectory) throws Exception {
        Pattern pattern = Pattern.compile("(?:,|\\n|^)(\"(?:(?:\"\")*[^\"]*)*\"|[^\",\\n]*|(?:\\n|$))");
        try (FileReader fileReader = new FileReader(csvFile, StandardCharsets.UTF_16)) {
            try (BufferedReader reader = new BufferedReader(fileReader)) {

                Set<String> done = new HashSet<>();

                String line;

                reader.readLine(); // header
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> cells = new ArrayList<>();
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        if (matcher.groupCount() >= 1) {
                            String rawValue = matcher.group(1);
                            while (rawValue.startsWith("\""))
                                rawValue = rawValue.substring(1);
                            while (rawValue.endsWith("\"")) {
                                rawValue = rawValue.substring(0, rawValue.length() - 1);
                            }
                            cells.add(rawValue);
                        } else
                            cells.add("");
                    }

                    String keyword = cells.get(10).replaceAll(" ", "");
                    String partNumberFilename = cells.get(4).trim();
                    String pakNumberfilename = cells.get(5).trim();
                    Set<String> filesToTry = new HashSet<>();
                    filesToTry.add(pakNumberfilename + ".pak");
                    filesToTry.add(partNumberFilename + ".pak");
                    filesToTry.add(pakNumberfilename + ".pk2");
                    filesToTry.add(partNumberFilename + ".pk2");

                    for (String filename : filesToTry) {
                        String setKey = filename + "." + keyword;
                        if (!done.contains(setKey)) {
                            try {
                                decryptFile(keyword, pakDirectory + File.separator + filename);
                                done.add(setKey);
                            } catch (BadPaddingException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void decryptFile(String keywordString, String pakFile) throws IOException, GeneralSecurityException {
        if (!new File(pakFile).exists()) return;
        System.out.println("Decrypt " + pakFile + " with keyword " + keywordString + "...");

        try (RandomAccessFile raf = new RandomAccessFile(pakFile, "r")) {
            byte[] search = new byte[anchor.length];
            boolean locked = false;
            for (int offs = 0; offs < raf.length() - anchor.length; offs ++) {
                raf.seek(offs);
                raf.read(search, 0, search.length);
                if (Arrays.equals(anchor, search)) {
                    System.out.println(" Found anchor at offs " + offs + "...");
                    locked = true;
                    break;
                }
            }

            if (locked) {
                List<PakSection> sections = new ArrayList<>();
                PakSection section;
                while (true) {
                    section = decryptSection(raf, keywordString);
                    sections.add(section);

                    if (section.opcode == 0x8001) {
                        // continue with current class
                        continue;
                    } else if (section.opcode == 0xFFFF) {
                        // stop, change class
                        int unused = raf.readUnsignedShort();
                        String cpp_class = readString(raf);
                        System.out.println(" Class=" + cpp_class);
                        continue;
                    } else if (section.opcode == 0x0000) {
                        // EOF
                        break;
                    }
                }

                if (raf.length() - raf.getFilePointer() != 0) {
                    throw new IllegalStateException("Didn't fully read PAK file");
                }

                for (PakSection recovered : sections) {
                    String folder = pakFile + "." + keywordString + "/";
                    new File(folder).mkdirs();
                    String clearFilename = folder + recovered.filename;
                    try (OutputStream writer = new FileOutputStream(clearFilename)) {
                        writer.write(recovered.body);
                    }
                }
            } else {
                System.out.println("Failed to find expected file anchor!");
            }
        }
    }

    private static class PakSection {
        private String filename;
        private byte[] header;
        private long start, end;
        private byte[] body;
        private int opcode;

        private PakSection(String filename, byte[] header, byte[] body, long start, long end,
                           int opcode) {
            this.filename = filename;
            this.header = header;
            this.start = start;
            this.end = end;
            this.body = body;
            this.opcode = opcode;
        }

        public String getFilename() {
            return filename;
        }

        public byte[] getHeader() {
            return header;
        }

        public byte[] getBody() {
            return body;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public int getOpcode() {
            return opcode;
        }
    }

    private static String readString(RandomAccessFile file) throws IOException {
        int length = readLength(file);
        byte[] stringData = new byte[length];
        file.read(stringData);
        return new String(stringData);
    }

    // See: https://github.com/pixelspark/corespark/blob/master/Libraries/atlmfc/src/mfc/arccore.cpp
    private static int readLength(RandomAccessFile file) throws IOException {
        int length;
        byte[] wCount = new byte[2];
        file.read(wCount);
        if (wCount[0] != (byte)0xFF || wCount[1] != (byte)0xFF) {
            length = (wCount[0] & 0xFF | ((wCount[1] << 8) & 0xFF00)) & 0xFFFF;

            if (length < 0) {
                throw new IllegalArgumentException(Integer.toString(length)
                        + ": " + Frame.toHexString(wCount));
            }
        } else {
            byte[] dwCount = new byte[4];
            file.read(dwCount);

            length = (dwCount[0] & 0xFF) |
                    ((dwCount[1] << 8) & 0xFF00) |
                    ((dwCount[2] << 16) & 0xFF0000) |
                    ((dwCount[3] << 24) & 0xFF000000);

            if (length < 0) {
                throw new IllegalArgumentException(Integer.toString(length)
                        + ": " + Frame.toHexString(dwCount));
            }
        }
        return length;
    }

    private static PakSection decryptSection(RandomAccessFile file, String keywordString)
            throws GeneralSecurityException, IOException {
        int fileNameLength = file.readUnsignedByte();
        byte[] fileName = new byte[fileNameLength];
        file.read(fileName);
        byte[] headerBytes = new byte[4];
        file.read(headerBytes);

        int length = readLength(file);

        long start = file.getFilePointer(), end = start + length;

        String filenameString = new String(fileName, StandardCharsets.US_ASCII);
        System.out.println(" Decrypting file " + filenameString
                + "(len=" + length + ") at range " + start + " - " + end + "...");

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        byte[] block = new byte[length];
        int read = file.read(block);
        Cipher rc2 = CryptoAPI.createRC2(keywordString);
        block = rc2.doFinal(block, 0, read);
        body.write(block);

        byte[] opcodeBytes = new byte[2];
        file.read(opcodeBytes);
        int opcode = (opcodeBytes[0] | ((opcodeBytes[1] << 8) & 0xFF00)) & 0xFFFF;
        return new PakSection(filenameString, headerBytes, body.toByteArray(), start, end, opcode);
    }

}
