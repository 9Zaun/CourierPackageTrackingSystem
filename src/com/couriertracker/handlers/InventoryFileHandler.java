package com.couriertracker.handlers;

import com.couriertracker.models.TrackingRecord;

public class InventoryFileHandler extends FileHandler {
    private static final String HEADER = "recordId,packageId,location,status,timestamp,handlerName";

    public InventoryFileHandler() {
        super("inventory_log.csv");
    }

    @Override
    protected String getHeader() {
        return HEADER;
    }

    public void logEvent(TrackingRecord record) {
        appendRow(record.toCsvRow());
    }
}