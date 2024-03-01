package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.windows.CryptoAPI;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.manevolent.atlas.windows.CryptoAPI.createRC2;

public class PakFile {

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
                    filesToTry.add(pakNumberfilename);
                    filesToTry.add(partNumberFilename);

                    if (!pakNumberfilename.equals("22611BC090")) continue;

                    for (String filename : filesToTry) {
                        String setKey = filename + "." + keyword;
                        if (!done.contains(setKey)) {
                            decryptFile(keyword, pakDirectory + File.separator + filename + ".pak");
                            done.add(setKey);
                        }
                    }
                }
            }
        }
    }

    private static void decryptFile(String keywordString, String pakFile) throws IOException, GeneralSecurityException {
        if (!new File(pakFile).exists()) return;
        System.out.println("Decrypt " + pakFile + " with keyword " + keywordString + "...");
        StringBuilder sb = new StringBuilder();
        Cipher rc2 = null;
        try (InputStream inputStream = new FileInputStream(pakFile)) {
            byte[] cipherText = inputStream.readAllBytes();

            boolean locked = false;
            int windowSize = 16;
            List<byte[]> priorWindows = new ArrayList<>();

            for (int offs = 0; offs < cipherText.length - 8; offs += (locked ? windowSize : 1)) {
                int realWindowSize = Math.min(cipherText.length - offs, windowSize);
                byte[] window = new byte[realWindowSize];
                if (!locked) {
                    rc2 = createRC2(keywordString.replaceAll(" ", ""));
                    assert rc2.getBlockSize() == CryptoAPI.RC2_BLOCK_LENGTH;
                }

                System.arraycopy(cipherText, offs, window, 0, realWindowSize);
                try {
                    window = rc2.update(window, 0, realWindowSize);

                    priorWindows.add(window);
                    while (priorWindows.size() > 9) {
                        priorWindows.remove(0);
                    }

                    if (window.length <= 0) {
                        locked = false;
                    } else {
                        System.out.println(Frame.toHexString(window));
                        boolean good = true;
                        String windowString = new String(window);
                        for (int i = 0; i < window.length; i++) {
                            if (!Character.isLetterOrDigit(window[i]) && !Character.isWhitespace(window[i])) {
                                good = false;
                                break;
                            }
                        }
                        if (good) {
                            // From 22611BC090 (2009MY Forester),
                            //S2140A306096554F6E96554F6E96554F6E96554F6EB1
                            //chksum:                                   B1
                            //data:     96554F6E96554F6E96554F6E96554F6E
                            //    0A3060: suspect this is offset
                            //S214 :unsure ('S' is 0x53, or dec 83, '2' is 0x32 or dec 50), 0x14 is dec 20

                            if (!locked && !windowString.startsWith("S")) {
                                good = false;
                            } else {
                                sb.append(windowString);
                            }
                        }
                        locked = good;
                    }
                } catch (Exception ex) {
                    if (locked)
                        locked = false;
                }
            }
        }

        if (sb.length() > 0) {
            System.out.println("Decrypted " + sb.length() + " characters");
            try (FileWriter writer = new FileWriter(pakFile + "." + keywordString + ".clear")) {
                writer.write(sb.toString());
            }
        } else {
            System.out.println("Failed to decrypt!");
        }
    }

}
