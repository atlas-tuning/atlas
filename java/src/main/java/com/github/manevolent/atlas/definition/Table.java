package com.github.manevolent.atlas.definition;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.manevolent.atlas.definition.Axis.X;
import static com.github.manevolent.atlas.definition.Axis.Y;

public class Table {
    private String name;
    private Series data;
    private Map<Axis, Series> axes;

    public float getCell(Map<Axis, Integer> coordinates) throws IOException {
        int index = coordinates.get(X);

        if (coordinates.containsKey(Y)) {
            index += coordinates.get(Y) * axes.get(X).getLength();
        }

        return data.get(index);
    }

    public float getCell(Integer... coordinates) throws IOException {
        Map<Axis, Integer> coordinatesMap = new HashMap<>();

        for (int n = 0; n < coordinates.length; n ++) {
            int finalN = n;
            Axis axis = Arrays.stream(Axis.values())
                    .filter(a -> a.getIndex() == finalN)
                    .findFirst().orElseThrow();

            coordinatesMap.put(axis, coordinates[n]);
        }

        return getCell(coordinatesMap);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(Series data) {
        this.data = data;
    }

    public Series getData() {
        return this.data;
    }

    public void setAxes(Map<Axis, Series> axes) {
        this.axes = axes;
    }

    public void setAxis(Axis axis, Series series) {
        this.axes.put(axis, series);
    }

    public Series getSeries(Axis axis) {
        return this.axes.get(axis);
    }

    public void writeCsv(OutputStream outputStream, int rounding_precision) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                BufferedWriter writer = new BufferedWriter(osw)) {
            Consumer<String> writeCell = (value) -> {
                try {
                    writer.write("\"" + value + "\",");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            Series y = axes.get(Y);
            Series x = axes.get(X);

            writeCell.accept("");

            if (axes.size() > 0) {
                for (int x_index = 0; x_index < x.getLength(); x_index++) {
                    writeCell.accept(String.format("%." + rounding_precision + "f", x.get(x_index)));
                }
            }
            writer.write("\r\n");

            if (axes.size() == 2) {
                for (int y_index = 0; y_index < y.getLength(); y_index ++) {
                    // Write the row header
                    writeCell.accept(String.format("%." + rounding_precision + "f", y.get(y_index)));
                    for (int x_index = 0; x_index < x.getLength(); x_index ++) {
                        // Write the cell data
                        writeCell.accept(String.format("%." + rounding_precision + "f", getCell(x_index, y_index)));
                    }
                    writer.write("\r\n");
                }
            } else if (axes.size() == 1) {
                // Write the row header
                writeCell.accept("");
                for (int x_index = 0; x_index < x.getLength(); x_index ++) {
                    // Write the cell data
                    writeCell.accept(String.format("%." + rounding_precision + "f", getCell(x_index)));
                }
                writer.write("\r\n");
            } else if (axes.size() == 0) {
                // Write the row header
                writeCell.accept("");
                x = data;
                for (int x_index = 0; x_index < x.getLength(); x_index ++) {
                    // Write the cell data
                    writeCell.accept(String.format("%." + rounding_precision + "f", getCell(x_index)));
                }
                writer.write("\r\n");
            }

            writeCell.accept("");
            writer.write("\r\n");
            writeCell.accept("Series"); writeCell.accept("Name"); writeCell.accept("Unit");
            writer.write("\r\n");
            writeCell.accept("Table"); writeCell.accept(name);
            if (data.getUnit() != null) {
                writeCell.accept(data.getUnit().name());
            } else {
                writeCell.accept("Unknown!");
            }
            writer.write("\r\n");

            List<Axis> axes = new ArrayList<>(this.axes.keySet());
            axes.sort(Comparator.comparing(Axis::name));
            for (Axis axis : axes) {
                writeCell.accept(axis.name() + " Axis");
                Series series = this.axes.get(axis);
                writeCell.accept(series.getName());
                if (series.getUnit() == null) {
                    writeCell.accept("Unknown!");
                } else {
                    writeCell.accept(this.axes.get(axis).getUnit().name());
                }
                writer.write("\r\n");
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private final Table table = new Table();

        public Builder() {
            table.setAxes(new HashMap<>());
        }

        public Builder withName(String name) {
            table.setName(name);
            return this;
        }

        public Builder withData(Series series) {
            table.setData(series);
            return this;
        }

        public Builder withData(Series.Builder series) {
            return withData(series.build());
        }

        public Builder withAxis(Axis axis, Series series) {
            table.setAxis(axis, series);
            return this;
        }

        public Builder withAxis(Axis axis, Series.Builder series) {
            return withAxis(axis, series.build());
        }

        public Table build() {
            int n = 1;
            for (Axis axis : table.axes.keySet()) {
                n *= table.getSeries(axis).getLength();
            }
            table.data.setLength(n);
            return table;
        }
    }
}
