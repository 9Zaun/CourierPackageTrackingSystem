package com.couriertracker.models;

import com.couriertracker.enums.PackageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TrackingRecord {
    private String recordID;
    private String packageID;
    private String location;
    private PackageStatus status;
    private LocalDateTime timestamp;
    private DeliveryAgent handledBy;

    public TrackingRecord(String packageID, String location, PackageStatus status, DeliveryAgent handledBy) {
        this.recordID = UUID.randomUUID().toString();
        this.packageID = packageID;
        this.location = location;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.handledBy = handledBy;
    }

    public String getRecordID() {
        return recordID;
    }

    public String getPackageID() {
        return packageID;
    }

    public String getLocation() {
        return location;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public DeliveryAgent getHandledBy() {
        return handledBy;
    }

    public String toCsvRow() {
        String handlerName = handledBy != null ? handledBy.getName() : "";
        return recordID + "," + packageID + "," + location + "," + status + "," + timestamp + "," + handlerName;
    }
}
