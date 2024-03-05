package com.github.manevolent.atlas.definition;

import java.util.*;
import java.util.stream.Collectors;

public class Rom {
    private Vehicle vehicle;
    private List<FlashRegion> regions;
    private FlashMethod flashMethod;
    private List<Table> tables;
    private Set<Scale> scales;

    public Rom() {

    }

    private void setScales(LinkedHashSet<Scale> scales) {
        this.scales = scales;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<FlashRegion> getRegions() {
        return regions;
    }

    public void setRegions(List<FlashRegion> regions) {
        this.regions = regions;
    }

    public FlashMethod getFlashMethod() {
        return flashMethod;
    }

    public void setFlashMethod(FlashMethod flashMethod) {
        this.flashMethod = flashMethod;
    }

    public List<Table> getTables() {
        return tables;
    }

    public Table findTableByName(String name) {
        return tables.stream()
                .filter(table -> table.getName() != null && table.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(name));
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<Scale> getScales() {
        return scales;
    }

    public static class Builder {
        private final Rom rom = new Rom();

        public Builder() {
            rom.setTables(new ArrayList<>());
            rom.setRegions(new ArrayList<>());
            rom.setScales(new LinkedHashSet<>());
        }

        public Builder withScales(Scale.Builder... scales) {
            rom.scales.addAll(Arrays.stream(scales).map(Scale.Builder::build).toList());
            return this;
        }

        public Builder withScales(Scale... scales) {
            rom.scales.addAll(Arrays.asList(scales));
            return this;
        }

        public Builder withTables(Table... tables) {
            Arrays.stream(tables).forEach(this::withTable);
            return this;
        }

        public Builder withTables(Table.Builder... builders) {
            Arrays.stream(builders).forEach(this::withTable);
            return this;
        }

        public Builder withFlashMethod(FlashMethod method) {
            this.rom.setFlashMethod(method);
            return this;
        }

        public Builder withTable(Table table) {
            // Verify scales are registered
            Set<Scale> unknownScales = new HashSet<>();
            unknownScales.addAll(table.getAxes().stream()
                    .map(table::getSeries)
                    .map(Series::getScale)
                    .filter(scale -> !this.rom.scales.contains(scale))
                    .toList());

            if (!this.rom.scales.contains(table.getData().getScale())) {
                unknownScales.add(table.getData().getScale());
            }

            if (!unknownScales.isEmpty()) {
                throw new IllegalArgumentException("Unknown scale(s) for table "
                        + table.getName() + ": " +
                        unknownScales.stream().map(Scale::toString)
                                .collect(Collectors.joining(", "))
                );
            }

            rom.getTables().add(table);
            return this;
        }

        public Builder withTable(Table.Builder table) {
            return withTable(table.build());
        }

        public Builder withRegions(FlashRegion... regions) {
            rom.getRegions().addAll(Arrays.asList(regions));
            return this;
        }

        public Builder withRegion(FlashRegion region) {
            rom.getRegions().add(region);
            return this;
        }

        public Builder withVehicle(Vehicle.Builder builder) {
            return withVehicle(builder.build());
        }

        public Builder withVehicle(Vehicle vehicle) {
            rom.setVehicle(vehicle);
            return this;
        }

        public Rom build() {
            return rom;
        }
    }
}
