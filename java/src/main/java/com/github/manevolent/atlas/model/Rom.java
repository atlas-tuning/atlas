package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.model.source.VehicleSource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Rom {
    private static final Set<String> acceptableClassNames = Collections.unmodifiableSet(Stream.of(
            Rom.class,
            VehicleSource.class,
            Scale.class,
            ScalingOperation.class,
            Series.class,
            Table.class,
            Unit.class,
            UnitClass.class,
            Vehicle.class,
            Precision.class,
            MemorySection.class,
            MemoryParameter.class,
            MemoryByteOrder.class,
            MemoryAddress.class,
            DataFormat.class,
            Axis.class,
            ArithmeticOperation.class,
            MemoryEncryptionType.class,
            KeyProperty.class
    ).map(Class::getName).collect(Collectors.toSet()));

    private Vehicle vehicle;
    private List<MemorySection> sections;
    private FlashMethod flashMethod;
    private List<Table> tables;
    private Set<Scale> scales;
    private Set<MemoryParameter> parameters;
    private Map<String, RomProperty> properties;

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

    public Set<MemoryParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<MemoryParameter> parameters) {
        this.parameters = parameters;
    }

    @SuppressWarnings("unchecked")
    public <T extends RomProperty> T getProperty(String name, Class<T> clazz) {
        return (T) properties.get(name);
    }

    public Map<String, RomProperty> getProperties() {
        return properties;
    }

    public boolean hasParameter(MemoryParameter parameter) {
        return parameters.contains(parameter);
    }

    public void addProperty(String name, RomProperty property) {
        properties.put(name, property);
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public Collection<RomProperty> getPropertyValues() {
        return properties.values();
    }

    public void setProperties(Map<String, RomProperty> map) {
        this.properties = map;
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public boolean hasProperty(RomProperty property) {
        return properties.containsValue(property);
    }

    public void saveToArchive(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            saveToArchive(fos);
        }
    }

    public void saveToArchive(OutputStream outputStream) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (MemorySection section : getSections()) {
                // Skip RAM sections
                if (section.getMemoryType() == MemoryType.RAM) {
                    continue;
                }

                zos.putNextEntry(new ZipEntry(section.getName() + ".bin"));
                section.copyTo(zos);
                zos.closeEntry();
            }

            Yaml yaml = new Yaml();
            String yamlString = yaml.dump(this);
            zos.putNextEntry(new ZipEntry("project.yaml"));
            byte[] yamlData = yamlString.getBytes(StandardCharsets.UTF_16);
            zos.write(yamlData);
            zos.closeEntry();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Rom rom = new Rom();

        public Builder() {
            rom.setTables(new ArrayList<>());
            rom.setSections(new ArrayList<>());
            rom.setScales(new LinkedHashSet<>());
            rom.setParameters(new LinkedHashSet<>());
            rom.setProperties(new LinkedHashMap<>());

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

        public Builder withProperty(String name, RomProperty property) {
            rom.addProperty(name, property);
            return this;
        }

        public Rom build() {
            return rom;
        }
    }

    public static Rom loadFromArchive(InputStream inputStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream);
        String yamlString = null;
        Map<String, byte[]> sections = new HashMap<>();

        ZipEntry entry;
        while (((entry = zis.getNextEntry()) != null)) {
            if (entry.getName().equals("project.yaml")) {
                yamlString = new String(zis.readAllBytes(), StandardCharsets.UTF_16);
            } else if (entry.getName().endsWith(".bin")) {
                sections.put(entry.getName(), zis.readAllBytes());
            } else {
                // ignore
            }
        }

        TagInspector taginspector = tag -> acceptableClassNames.contains(tag.getClassName());

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setMaxAliasesForCollections(1024);
        loaderOptions.setNestingDepthLimit(1024);
        loaderOptions.setTagInspector(taginspector);
        Yaml yaml = new Yaml(loaderOptions);
        Rom rom = yaml.load(yamlString);

        for (MemorySection section : rom.getSections()) {
            byte[] data = sections.get(section.getName() + ".bin");
            section.setup(rom, data);
        }

        return rom;
    }

}
