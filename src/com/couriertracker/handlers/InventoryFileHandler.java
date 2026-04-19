package com.couriertracker.handlers;

import com.couriertracker.models.TrackingRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class InventoryFileHandler {
    private static final String HEADER = "recordId,packageId,location,status,timestamp,handlerName";
    private final Path filePath;

    public InventoryFileHandler() {
        this.filePath = Paths.get("data", "inventory_log.csv");
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                Files.writeString(filePath, HEADER + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise inventory log", e);
        }
    }

    public void logEvent(TrackingRecord record) {
        try {
            Files.writeString(filePath, record.toCsvRow() + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append inventory log", e);
        }
    }
}
