package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.source.ArraySource;
import com.github.manevolent.atlas.model.source.LazySource;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Project {
    private Vehicle vehicle;
    private List<MemorySection> sections;
    private ConnectionType connectionType;
    private List<Table> tables;
    private Set<Scale> scales;
    private Set<MemoryParameter> parameters;
    private Map<String, ProjectProperty> properties;
    private List<Calibration> calibrations;

    public Project() {

    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return this.connectionType;
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

    public void setScales(Set<Scale> scales) {
        this.scales = scales;
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

    public MemoryAddress getDefaultMemoryAddress() {
        MemorySection first = sections.getFirst();
        return MemoryAddress.builder()
                .withSection(first)
                .withOffset(first.getBaseAddress()).build();
    }

    public void removeParameter(MemoryParameter parameter) {
        parameters.remove(parameter);
    }

    public Set<MemoryParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<MemoryParameter> parameters) {
        this.parameters = parameters;
    }

    @SuppressWarnings("unchecked")
    public <T extends ProjectProperty> T getProperty(String name, Class<T> clazz) {
        return (T) properties.get(name);
    }

    public ProjectProperty getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, ProjectProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, ProjectProperty> map) {
        this.properties = map;
    }

    public boolean hasParameter(MemoryParameter parameter) {
        return parameters.contains(parameter);
    }

    public void addProperty(String name, ProjectProperty property) {
        properties.put(name, property);
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public Collection<ProjectProperty> getPropertyValues() {
        return properties.values();
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public boolean hasProperty(ProjectProperty property) {
        return properties.containsValue(property);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<MemoryReference> getMemoryReferences() {
        List<MemoryReference> references = new ArrayList<>();

        getTables().forEach(table -> {
            if (table.getData() != null) {
                references.add(MemoryReference.of(table, table.getData()));
            }

            for (Series series : table.getAllAxes()) {
                references.add(MemoryReference.of(table, series));
            }
        });

        getParameters().forEach(parameter -> {
            references.add(MemoryReference.of(parameter));
        });

        return references;
    }

    public void addSection(MemorySection section) {
        sections.add(section);
    }

    public boolean removeSection(MemorySection section) {
        return sections.remove(section);
    }

    public boolean hasSection(MemorySection section) {
        return sections.contains(section);
    }

    public void addCalibration(Calibration calibration) {
        this.calibrations.add(calibration);
    }

    public boolean removeCalibration(Calibration calibration) {
        return calibrations.remove(calibration);
    }

    public boolean hasCalibration(Calibration calibration) {
        return calibrations.contains(calibration);
    }

    public List<Calibration> getCalibrations() {
        return calibrations;
    }

    public void setCalibrations(List<Calibration> calibrations) {
        this.calibrations = calibrations;
    }

    public static class Builder {
        private final Project project = new Project();

        public Builder() {
            project.setTables(new ArrayList<>());
            project.setSections(new ArrayList<>());
            project.setScales(new LinkedHashSet<>());
            project.setParameters(new LinkedHashSet<>());
            project.setProperties(new LinkedHashMap<>());
            project.setVehicle(new Vehicle());
            project.setCalibrations(new ArrayList<>());

            withScales(Scale.NONE);
        }

        public Builder withScales(Scale.Builder... scales) {
            project.scales.addAll(Arrays.stream(scales).map(Scale.Builder::build).toList());
            return this;
        }

        public Builder withScales(Scale... scales) {
            project.scales.addAll(Arrays.asList(scales));
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

        public Builder withFlashMethod(ConnectionType connectionType) {
            this.project.setConnectionType(connectionType);
            return this;
        }

        public Builder withTable(Table table) {
            // Verify scales are registered
            Set<Scale> unknownScales = new HashSet<>(table.getAxes().keySet().stream()
                    .map(table::getSeries)
                    .map(Series::getScale)
                    .filter(scale -> !this.project.scales.contains(scale))
                    .toList());

            if (!this.project.scales.contains(table.getData().getScale())) {
                unknownScales.add(table.getData().getScale());
            }

            if (!unknownScales.isEmpty()) {
                throw new IllegalArgumentException("Unknown scale(s) for table "
                        + table.getName() + ": " +
                        unknownScales.stream().map(Scale::toString)
                                .collect(Collectors.joining(", "))
                );
            }

            project.getTables().add(table);
            return this;
        }

        public Builder withTable(Table.Builder table) {
            return withTable(table.build());
        }

        public Builder withSections(MemorySection... sections) {
            project.getSections().addAll(Arrays.asList(sections));
            return this;
        }

        public Builder withSection(MemorySection section) {
            project.getSections().add(section);
            return this;
        }

        public Builder withSection(MemorySection.Builder section) {
            return withSection(section.build());
        }

        public Builder withVehicle(Vehicle.Builder builder) {
            return withVehicle(builder.build());
        }

        public Builder withVehicle(Vehicle vehicle) {
            project.setVehicle(vehicle);
            return this;
        }

        public Builder withParameter(MemoryParameter parameter) {
            project.addParameter(parameter);
            return this;
        }

        public Builder withParameter(MemoryParameter.Builder parameter) {
            return withParameter(parameter.build());
        }

        public Builder withProperty(String name, ProjectProperty property) {
            project.addProperty(name, property);
            return this;
        }

        public Builder withCalibration(Calibration calibration) {
            project.addCalibration(calibration);
            return this;
        }

        public Project build() {
            project.sections.forEach(x -> x.setup(project));

            return project;
        }
    }
}
