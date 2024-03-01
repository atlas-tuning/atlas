package com.github.manevolent.atlas.ssm4;

import java.io.*;
import java.util.*;

public class SubaruDITTableMapper {
    public static final List<Range> SUBARU_2022MY_USDM_WRX_MT = Arrays.asList(
            new Range(0x0013c468, 0x00146f7c)
    );

    enum Dimension {
        X, Y, Z, W
    }

    public static class TableDimension {
        private final Dimension dimension;
        private final int definition_address;
        private final int row_address;
        private final int size;

        public TableDimension(Dimension dimension, int definitionAddress, int rowAddress, int size) {
            this.dimension = dimension;
            definition_address = definitionAddress;
            row_address = rowAddress;
            this.size = size;
        }

        public Dimension getDimension() {
            return dimension;
        }

        public int getDefinitionAddress() {
            return definition_address;
        }

        public int getRowAddress() {
            return row_address;
        }

        public int getSize() {
            return size;
        }
    }

    public static class Table {
        private final int definition_address;
        private final Map<Dimension, TableDimension> dimensions;
        private final int data_address;

        public Table(int definitionAddress, int dataAddress, Map<Dimension, TableDimension> dimensions) {
            definition_address = definitionAddress;
            data_address = dataAddress;
            this.dimensions = dimensions;
        }

        public static Table read(RandomAccessFile raf) throws IOException {
            int table_definition_address = (int) (raf.getFilePointer());

            Map<Dimension, Integer> dimensionSizes = new LinkedHashMap<>();
            for (Dimension dimension : Dimension.values()) {
                int size = raf.readByte() & 0xFF;
                if (size > 0) {
                    dimensionSizes.put(dimension, size);
                }
            }

            Map<Dimension, TableDimension> dimensions = new LinkedHashMap<>();
            for (Dimension dimension : dimensionSizes.keySet()) {
                int size = dimensionSizes.get(dimension);
                int definition_address = (int) (raf.getFilePointer());
                int row_address = Integer.reverseBytes(raf.readInt());
                dimensions.put(dimension, new TableDimension(
                        dimension,
                        definition_address,
                        row_address,
                        size
                ));
            }

            int data_address = Integer.reverseBytes(raf.readInt());
            return new Table(
                    table_definition_address,
                    data_address,
                    dimensions
            );
        }

        public int getDefinitionAddress() {
            return definition_address;
        }

        public TableDimension getDimension(Dimension dimension) {
            return getDimensions().get(dimension);
        }

        public Map<Dimension, TableDimension> getDimensions() {
            return dimensions;
        }

        public boolean matchesSize(int... dimensionSizes) {
            for (int i = 0; i < dimensionSizes.length; i ++) {
                Dimension dimension;

                switch (i) {
                    case 0:
                        dimension = Dimension.X;
                        break;
                    case 1:
                        dimension = Dimension.Y;
                        break;
                    case 2:
                        dimension = Dimension.Z;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown dimension " + i);
                }

                TableDimension tableDimension = getDimension(dimension);

                if (tableDimension == null)
                    return false;

                if (tableDimension.getSize() != dimensionSizes[i])
                    return false;
            }

            return true;
        }

        public int getDataAddress() {
            return data_address;
        }

        public byte[] getData(RandomAccessFile raf, int num_bytes_per_data) throws IOException {
            int dimension_size = 1;
            for (Dimension dimension : dimensions.keySet()) {
                dimension_size *= dimensions.get(dimension).size;
            }

            byte[] data_range = new byte[num_bytes_per_data * dimension_size];
            raf.seek(data_address & 0xFFFFFF);
            raf.read(data_range);

            return data_range;
        }

        public boolean isEmpty(RandomAccessFile raf, int num_bytes_per_data) throws IOException {
            byte[] data_range = getData(raf, num_bytes_per_data);

            for (int i = 0; i < data_range.length; i ++) {
                if (data_range[i] != 0x0) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class Range {
        private final int start, end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public List<Table> read(RandomAccessFile raf) throws IOException {
            raf.seek(start);
            List<Table> tables = new ArrayList<>();
            while (raf.getFilePointer() < end) {
                Table table = Table.read(raf);

                long saved_offset = raf.getFilePointer();

                //if (!table.isEmpty(raf, 4)) {
                    tables.add(table);
                //}

                raf.seek(saved_offset);
            }
            return tables;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    private static String format_address(int address) {
        String string = Integer.toHexString(address).toUpperCase();
        while (string.length() < 8) {
            string = "0" + string;
        }
        return "0x" + string;
    }

    private static void write_symbol(BufferedWriter writer, String name, int address) throws IOException {
        writer.write(name);
        writer.write(" ");
        writer.write(format_address(address));
        writer.write("\n");
    }

    public static void main(String[] args) throws IOException {
        List<Table> tables = new ArrayList<>();

        List<Range> ranges = SUBARU_2022MY_USDM_WRX_MT;

        RandomAccessFile file = new RandomAccessFile(args[0], "rw");
        for (Range range: ranges) {
            tables.addAll(range.read(file));
        }

        File symbolFile = new File(args[1]);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(symbolFile))) {
            for (Table table : tables) {
                String table_name = "TABLE_" + format_address(table.getDefinitionAddress());
                write_symbol(writer, table_name, table.getDefinitionAddress());

                for (Dimension dimension : table.dimensions.keySet()) {
                    TableDimension tableDimension = table.dimensions.get(dimension);
                    write_symbol(writer, table_name + "_" + dimension.name().toUpperCase()
                            + "_DATA_PTR", tableDimension.getDefinitionAddress());
                    write_symbol(writer, table_name + "_" + dimension.name().toUpperCase()
                            + "_DATA", tableDimension.getRowAddress());
                }

                write_symbol(writer, table_name + "_DATA", table.getDataAddress());

                if (table.matchesSize(16, 16)) {
                    System.out.println(table);
                }
            }
        }

        System.out.println("Found " + tables.size() + " tables.");
    }

}
