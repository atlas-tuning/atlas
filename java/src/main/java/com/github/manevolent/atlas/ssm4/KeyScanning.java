package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.Frame;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyScanning {
    private static Set<byte[]> findKeys(File directory) throws IOException {
        File[] children = directory.listFiles();
        if (children == null) return Collections.emptySet();

        Set<byte[]> set = new HashSet<>();
        for (File child : children) {
            if (child.isDirectory()) {
                for (byte[] key : findKeys(child)) {
                    if (set.stream().noneMatch(k -> Arrays.equals(k, key))) {
                        set.add(key);
                    }
                }
                continue;
            }

            if (!child.getName().endsWith(".xml")) {
                continue;
            }

            // Read file
            String data;
            try (FileReader reader = new FileReader(child)) {
                StringWriter writer = new StringWriter();
                reader.transferTo(writer);
                data = writer.toString();
            }

            Pattern pattern = Pattern.compile("CryptKey=\"(.+?)\"");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                String keyString = matcher.group(1);
                keyString = keyString.replaceAll("\\$", "");
                keyString = keyString.replaceAll(",", "");
                byte[] key = Crypto.toByteArray(keyString);

                System.out.println(Frame.toHexString(key) + " found in " + child.getName());

                if (set.stream().noneMatch(k -> Arrays.equals(k, key))) {
                    set.add(key);
                }
            }
        }

        return set;
    }

    public static void main(String[] args) throws IOException {
        File directory = new File ("/Users/matt/Documents/git/atlas/files/ssm4/28.6");
        Set<byte[]> keys = findKeys(directory);
        for (byte[] key : keys) {
           System.out.println("Crypto.toByteArray(\"" + Frame.toHexString(key) + "\"),");
        }
    }
}
