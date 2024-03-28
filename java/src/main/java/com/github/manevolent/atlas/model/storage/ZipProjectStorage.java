package com.github.manevolent.atlas.model.storage;

import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.model.source.LazySource;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.behavior.ProgressListener;
import com.google.common.io.CharSource;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZipProjectStorage implements ProjectStorage {
    private static final Set<String> acceptableClassNames = Collections.unmodifiableSet(Stream.of(
            Project.class, Scale.class, ScalingOperation.class,
            Series.class, Table.class, Unit.class, UnitClass.class,
            Vehicle.class, Precision.class, MemorySection.class, MemoryParameter.class,
            MemoryByteOrder.class, MemoryAddress.class, DataFormat.class,
            Axis.class, ArithmeticOperation.class, MemoryEncryptionType.class, KeyProperty.class,
            Color.class, SecurityAccessProperty.class, ConnectionType.class,
            Calibration.class, UUID.class
    ).map(Class::getName).collect(Collectors.toSet()));

    private final ProgressListener listener;

    public ZipProjectStorage(ProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Project load(File file) throws IOException {
        String yamlString = null;
        Map<UUID, FileHeader> sections = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(file);) {
           for (ZipHeader header : zipFile.getFileHeaders()) {
               if (!(header instanceof FileHeader fileHeader)) {
                   continue;
               }

               String fileName = fileHeader.getFileName();

               if (fileName.equals("project.yaml")) {
                    yamlString = new String(zipFile.getInputStream(fileHeader).readAllBytes(),
                            StandardCharsets.UTF_16);
                } else if (fileName.endsWith(".bin")) {
                    String uuidString = fileName.replaceFirst("\\.bin$", "");

                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        sections.put(uuid, fileHeader);
                    } catch (Exception ex) {
                        Log.ui().log(Level.WARNING, "Problem opening calibration binary \"" + fileName + "\"", ex);
                    }
                } else {
                    Log.ui().log(Level.WARNING, "Unknown project file in ZIP: \"" + fileName + "\"");
                }
            }

            TagInspector taginspector = tag -> acceptableClassNames.contains(tag.getClassName());

            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setMaxAliasesForCollections(1024);
            loaderOptions.setNestingDepthLimit(1024);
            loaderOptions.setTagInspector(taginspector);
            Yaml yaml = new Yaml(loaderOptions);
            Project project = yaml.load(yamlString);

            if (project.getCalibrations() == null) {
                project.setCalibrations(new ArrayList<>());
            }

            for (Calibration calibration : project.getCalibrations()) {
                MemorySection section = calibration.getSection();
                if (section == null) {
                    continue;
                }

                FileHeader sectionEntry = sections.get(calibration.getUuid());
                calibration.updateSource(LazySource.fromZipEntry(file, sectionEntry, section));
            }

            project.getSections().forEach(x -> x.setup(project));

            return project;
        }
    }

    @Override
    public void save(Project project, File file) throws IOException {
        Yaml yaml = new Yaml();
        String yamlString = yaml.dump(project);

        try (ZipFile zipFile = new ZipFile(file)) {
            // Save calibrations
            for (Calibration calibration : project.getCalibrations()) {
                if (!calibration.hasData()) {
                    continue;
                }

                String fileName = calibration.getUuid().toString() + ".bin";
                FileHeader existingFile = zipFile.getFileHeader(fileName);

                MemorySource source = calibration.getSource();
                if (source instanceof LazySource lazySource && !lazySource.isDirty() && existingFile != null) {
                    // Don't modify the existing calibration data; it isn't dirty
                    continue;
                }

                byte[] data = source.readFully();
                ZipParameters calParameters = new ZipParameters();
                calParameters.setFileNameInZip(fileName);
                zipFile.addStream(new ByteArrayInputStream(data), calParameters);
            }

            ZipParameters yamlParameters = new ZipParameters();
            yamlParameters.setFileNameInZip("project.yaml");
            zipFile.addStream(CharSource.wrap(yamlString).asByteSource(StandardCharsets.UTF_16).openStream(),
                    yamlParameters);
        }

        project.getCalibrations().stream()
                .map(Calibration::getSource)
                .filter(source -> source instanceof LazySource)
                .map(source -> (LazySource) source)
                .forEach(source -> source.setDirty(false));
    }

    public static class Factory implements ProjectStorageFactory {
        @Override
        public ProjectStorage createStorage(ProgressListener progressListener) {
            return new ZipProjectStorage(progressListener);
        }
    }
}
