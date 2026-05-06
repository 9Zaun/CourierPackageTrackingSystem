package com.couriertracker.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public abstract class FileHandler {
    protected final Path filePath;

    public FileHandler(String fileName) {
        this.filePath = Paths.get("data", fileName);
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                Files.writeString(filePath, getHeader() + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise log file: " + fileName, e);
        }
    }

    protected abstract String getHeader();

    protected void appendRow(String row) {
        try {
            Files.writeString(filePath, row + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to log file", e);
        }
    }

    protected static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}