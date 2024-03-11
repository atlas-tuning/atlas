package com.github.manevolent.atlas.ui.util;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class Csv {
    public static void writeCell(String value, Writer writer) throws IOException {
        String escaped = StringEscapeUtils.escapeCsv(value);
        writer.write("\"" +escaped + "\",");
    }

    public static void writeRow(Writer writer, String... cells) throws IOException {
        for (String string : cells) {
            Csv.writeCell(string, writer);
        }
        writer.write("\r\n");
    }

    public static void writeRow(Writer writer, Collection<String> cells) throws IOException {
        for (String string : cells) {
            Csv.writeCell(string, writer);
        }
        writer.write("\r\n");
    }
}
