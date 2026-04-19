package com.couriertracker.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class TransactionFileHandler {
    private static final String HEADER =
            "eventType,packageId,customerName,destination,baseAmount,discountApplied,finalAmount,refundAmount,timestamp";
    private final Path filePath;

    public TransactionFileHandler() {
        this.filePath = Paths.get("data", "transactions.csv");
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                Files.writeString(filePath, HEADER + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise transactions log", e);
        }
    }

    public void logPayment(String packageId, String customerName, String destination,
                           double baseAmount, double discountApplied, double finalAmount) {
        String row = String.join(",",
                "PAYMENT",
                escapeCsv(packageId),
                escapeCsv(customerName),
                escapeCsv(destination),
                Double.toString(baseAmount),
                Double.toString(discountApplied),
                Double.toString(finalAmount),
                "",
                LocalDateTime.now().toString());
        appendRow(row);
    }

    public void logDeliveryConfirmed(String packageId, String customerName, String destination) {
        String row = String.join(",",
                "DELIVERY_CONFIRMED",
                escapeCsv(packageId),
                escapeCsv(customerName),
                escapeCsv(destination),
                "",
                "",
                "",
                "",
                LocalDateTime.now().toString());
        appendRow(row);
    }

    public void logRefund(String packageId, String customerName, double refundAmount) {
        String row = String.join(",",
                "REFUND",
                escapeCsv(packageId),
                escapeCsv(customerName),
                "",
                "",
                "",
                "",
                Double.toString(refundAmount),
                LocalDateTime.now().toString());
        appendRow(row);
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void appendRow(String row) {
        try {
            Files.writeString(filePath, row + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append transaction log", e);
        }
    }
}
