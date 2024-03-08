package com.github.manevolent.atlas.model;

import java.util.*;
import java.util.stream.Collectors;

public class Rom {
    private Vehicle vehicle;
    private List<MemorySection> sections;
    private FlashMethod flashMethod;
    private List<Table> tables;
    private Set<Scale> scales;
    private Set<MemoryParameter> parameters;

    public Rom() {

    }

    private void setScales(Set<Scale> scales) {
        this.scales = scales;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<MemorySection> getSections() {
        return sections;
    }

    public void setSections(List<MemorySection> sections) {
        this.sections = sections;
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

    public Set<Scale> getScales() {
        return scales;
    }

    public boolean hasTable(Table table) {
        return tables.contains(table);
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public void removeTable(Table toDelete) {
        tables.remove(toDelete);
    }

    public void addParameter(MemoryParameter parameter) {
        if (!scales.contains(parameter.getScale())) {
            throw new IllegalArgumentException("Unknown scale(s) for parameter " + parameter.toString() + ": " +
                    parameter.getScale().toString());
        }
        parameters.add(parameter);
    }

    public void removeParameter(MemoryParameter parameter) {
        parameters.remove(parameter);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<MemoryParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<MemoryParameter> parameters) {
        this.parameters = parameters;
    }

    public boolean hasParameter(MemoryParameter parameter) {
        return parameters.contains(parameter);
    }

    public static class Builder {
        private final Rom rom = new Rom();

        public Builder() {
            rom.setTables(new ArrayList<>());
            rom.setSections(new ArrayList<>());
            rom.setScales(new LinkedHashSet<>());
            rom.setParameters(new LinkedHashSet<>());

            withScales(Scale.NONE);
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

        public Builder withSections(MemorySection... sections) {
            rom.getSections().addAll(Arrays.asList(sections));
            return this;
        }

        public Builder withSection(MemorySection section) {
            rom.getSections().add(section);
            return this;
        }

        public Builder withSection(MemorySection.Builder section) {
            return withSection(section.build());
        }

        public Builder withVehicle(Vehicle.Builder builder) {
            return withVehicle(builder.build());
        }

        public Builder withVehicle(Vehicle vehicle) {
            rom.setVehicle(vehicle);
            return this;
        }

        public Builder withParameter(MemoryParameter parameter) {
            rom.addParameter(parameter);
            return this;
        }

        public Builder withParameter(MemoryParameter.Builder parameter) {
            return withParameter(parameter.build());
        }

        public Rom build() {
            return rom;
        }
    }
}
