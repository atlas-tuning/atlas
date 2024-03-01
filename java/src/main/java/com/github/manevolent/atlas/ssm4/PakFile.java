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
 * [0]: ".../flashwrite/"
 * [1] ".../flashwrite/EcuData/"
 *
 * ..outputs files like this:
 * .../flashwrite/EcuData/FILENAME.pak.KEYWORD/(EcuDataMap,PcVerData,flash binaries,etc.)
 */
public class PakFile {
    public static void main(String[] args) throws Exception {
        File directory = new File(args[0]);
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".csv")) {
                System.out.println("Process " + file.getName() + "...");
                decrypt(file.getAbsolutePath(), args[1]);
            }
        }
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
                    if (keyword.length() <= 0) {
                        continue;
                    }

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

        try (RandomAccessFile file = new RandomAccessFile(pakFile, "r")) {
            byte[] headerBytes = new byte[4];
            file.read(headerBytes);

            List<PakSection> sections = new ArrayList<>();
            PakSection first = new PakSection("header.csv");
            readSectionBody(first, file, keywordString);
            sections.add(first);

            int unknown = file.readUnsignedShort();

            PakSection section;
            int opcode = readOpCode(file);
            while (true) {
                if (opcode == 0x8001) {
                    // continue with current class
                } else if (opcode == 0xFFFF) {
                    // stop, change class
                    int unused = file.readUnsignedShort();
                    String cpp_class = PakFile.readString(file);
                    System.out.println(" Class=" + cpp_class);
                } else if (opcode == 0x0000) {
                    // EOF
                    break;
                }

                section = readSection(file, keywordString);
                sections.add(section);

                opcode = section.opcode;
            }

            if (file.length() - file.getFilePointer() != 0) {
                throw new IllegalStateException("Didn't fully read PAK file");
            }

            for (PakSection recovered : sections) {
                String folder = pakFile + "." + keywordString + "/";
                new File(folder).mkdirs();
                String clearFilename = folder + recovered.filename;

                    try {
                        String specificKeyword = recovered.filename.equals("header.csv") ?
                                "CsvKey" :
                                keywordString;
                        Cipher rc2 = CryptoAPI.createRC2(specificKeyword);
                        recovered.body = rc2.doFinal(recovered.body, 0, recovered.body.length);

                        try (OutputStream writer = new FileOutputStream(clearFilename)) {
                            System.out.println(" Writing " + clearFilename + "...");
                            writer.write(recovered.body);
                        }
                    } catch (Exception ex) {
                        System.err.println(" Problem decrypting " + recovered.filename + ": " + ex.getMessage());
                    }
            }
        }
    }

    private static class PakSection {
        private String filename;
        private byte[] header;
        private long start, end;
        private byte[] body;
        private int opcode;

        private PakSection(String filename) {
            this.filename = filename;
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

    private static int readOpCode(RandomAccessFile file) throws IOException {
        byte[] opcodeBytes = new byte[2];
        file.read(opcodeBytes);
        return (opcodeBytes[0] | ((opcodeBytes[1] << 8) & 0xFF00)) & 0xFFFF;
    }

    private static PakSection readSection(RandomAccessFile file, String keywordString)
            throws GeneralSecurityException, IOException {
        int fileNameLength = file.readUnsignedByte();
        byte[] fileName = new byte[fileNameLength];
        file.read(fileName);
        byte[] headerBytes = new byte[4];
        file.read(headerBytes);

        String fileNameString = new String(fileName, StandardCharsets.US_ASCII);
        PakSection section = new PakSection(fileNameString);
        readSectionBody(section, file, keywordString);

        section.opcode = readOpCode(file);
        return section;
    }

    private static PakSection readSectionBody(PakSection section,
                                                 RandomAccessFile file, String keywordString)
            throws GeneralSecurityException, IOException {
        int length = readLength(file);

        long start = file.getFilePointer(), end = start + length;

        String filenameString = section.filename;
        System.out.println(" Reading section " + filenameString
                + " (len=" + length + ") at range " + start + " - " + end + "...");

        byte[] block = new byte[length];
        int read = file.read(block);
        if (read != length) {
            throw new EOFException();
        }
        section.body = block;

        return section;
    }

}
