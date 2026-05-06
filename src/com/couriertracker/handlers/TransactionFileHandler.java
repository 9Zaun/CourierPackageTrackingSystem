package com.couriertracker.handlers;

import java.time.LocalDateTime;

public class TransactionFileHandler extends FileHandler {
    private static final String HEADER =
            "eventType,packageId,customerName,destination,baseAmount,discountApplied,finalAmount,refundAmount,timestamp";

    public TransactionFileHandler() {
        super("transactions.csv");
    }

    @Override
    protected String getHeader() {
        return HEADER;
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
                "", "", "", "",
                LocalDateTime.now().toString());
        appendRow(row);
    }

    public void logRefund(String packageId, String customerName, double refundAmount) {
        String row = String.join(",",
                "REFUND",
                escapeCsv(packageId),
                escapeCsv(customerName),
                "", "", "", "",
                Double.toString(refundAmount),
                LocalDateTime.now().toString());
        appendRow(row);
    }
}