package com.github.manevolent.atlas.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.function.Consumer;

import static com.github.manevolent.atlas.model.Axis.X;
import static com.github.manevolent.atlas.model.Axis.Y;

public class Table {
    private String name;
    private Series data;
    private Map<Axis, Series> axes;

    public Table(String name) {
        this.name = name;
    }

    public Table() {

    }

    public Map<Axis, Series> getAxes() {
        return axes;
    }

    public float getCell(MemorySource source, Map<Axis, Integer> coordinates) throws IOException {
        return data.get(source, getDataIndex(coordinates));
    }

    public float getCell(MemorySource source, Integer... coordinates) throws IOException {
        Map<Axis, Integer> coordinatesMap = new HashMap<>();

        for (int n = 0; n < coordinates.length; n ++) {
            int finalN = n;
            Axis axis = Arrays.stream(Axis.values())
                    .filter(a -> a.getIndex() == finalN)
                    .findFirst().orElseThrow();

            coordinatesMap.put(axis, coordinates[n]);
        }

        return getCell(source, coordinatesMap);
    }

    public float setCell(MemorySource source, float value, Map<Axis, Integer> coordinates) throws IOException {
        return data.set(source, getDataIndex(coordinates), value);
    }

    public float setCell(MemorySource source, float value, Integer... coordinates) throws IOException {
        Map<Axis, Integer> coordinatesMap = new HashMap<>();

        for (int n = 0; n < coordinates.length; n ++) {
            int finalN = n;
            Axis axis = Arrays.stream(Axis.values())
                    .filter(a -> a.getIndex() == finalN)
                    .findFirst().orElseThrow();

            coordinatesMap.put(axis, coordinates[n]);
        }

        return setCell(source, value, coordinatesMap);
    }

    public int getDataIndex(Map<Axis, Integer> coordinates) {
        if (coordinates.isEmpty()) {
            return 0;
        }

        int index = coordinates.get(X);

        if (coordinates.containsKey(Y)) {
            int y_index = coordinates.get(Y);
            if (y_index > 0) {
                index += y_index * axes.get(X).getLength();
            }
        }

        return index;
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

    public void writeCsv(MemorySource source, OutputStream outputStream, int rounding_precision) throws IOException {
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

            if (!axes.isEmpty()) {
                for (int x_index = 0; x_index < x.getLength(); x_index++) {
                    writeCell.accept(String.format("%." + rounding_precision + "f", x.get(source, x_index)));
                }
            }
            writer.write("\r\n");

            if (axes.size() == 2) {
                for (int y_index = 0; y_index < y.getLength(); y_index ++) {
                    // Write the row header
                    writeCell.accept(String.format("%." + rounding_precision + "f", y.get(source, y_index)));
                    for (int x_index = 0; x_index < x.getLength(); x_index ++) {
                        // Write the cell data
                        writeCell.accept(String.format("%." + rounding_precision + "f", getCell(source, x_index, y_index)));
                    }
                    writer.write("\r\n");
                }
            } else if (axes.size() == 1) {
                // Write the row header
                writeCell.accept("");
                for (int x_index = 0; x_index < x.getLength(); x_index ++) {
                    // Write the cell data
                    writeCell.accept(String.format("%." + rounding_precision + "f", getCell(source, x_index)));
                }
                writer.write("\r\n");
            } else if (axes.isEmpty()) {
                // Write the row header
                writeCell.accept("");
                x = data;
                for (int x_index = 0; x_index < x.getLength(); x_index ++) {
                    // Write the cell data
                    writeCell.accept(String.format("%." + rounding_precision + "f", getCell(source, x_index)));
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

    public boolean hasAxis(Axis axis) {
        return axes.containsKey(axis);
    }

    public Series removeAxis(Axis axis) {
        return axes.remove(axis);
    }

    /**
     * Used when editing tables
     * @return
     */
    public Table copy() {
        Table copy = new Table();

        copy.axes = new HashMap<>();
        for (Axis axis : axes.keySet()) {
            copy.axes.put(axis, axes.get(axis).copy());
        }

        copy.name = name;
        copy.data = data.copy();

        return copy;
    }

    public void apply(Table changed) {
        this.axes.clear();
        for (Axis axis : changed.axes.keySet()) {
            this.axes.put(axis, changed.axes.get(axis).copy());
        }

        this.data = changed.data.copy();
        this.name = changed.name;
    }

    public boolean hasScale(Scale scale) {
        if (getData().getScale() == scale) {
            return true;
        }

        for (Axis axis : axes.keySet()) {
            if (getSeries(axis).getScale() == scale) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void updateLength() {
        int n = 1;
        for (Axis axis : axes.keySet()) {
            n *= getSeries(axis).getLength();
        }
        data.setLength(n);
    }

    public Collection<Series> getAllAxes() {
        return axes.values();
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
            table.updateLength();
            return table;
        }
    }
}
