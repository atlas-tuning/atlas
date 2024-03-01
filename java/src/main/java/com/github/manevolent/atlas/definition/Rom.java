package com.github.manevolent.atlas.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rom {
    private Vehicle vehicle;
    private List<FlashRegion> regions;
    private FlashMethod flashMethod;
    private List<Table> tables;

    public Rom() {

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

    public static class Builder {
        private final Rom rom = new Rom();

        public Builder() {
            rom.setTables(new ArrayList<>());
            rom.setRegions(new ArrayList<>());
        }

        public Builder withTables(Table... tables) {
            rom.getTables().addAll(Arrays.asList(tables));
            return this;
        }

        public Builder withFlashMethod(FlashMethod method) {
            this.rom.setFlashMethod(method);
            return this;
        }

        public Builder withTable(Table table) {
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

        public Rom build() {
            return rom;
        }
    }
}
