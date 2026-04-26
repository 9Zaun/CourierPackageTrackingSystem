package com.couriertracker.models;

import com.couriertracker.enums.PackageStatus;

public interface PackageType {
    String getPackageID();
    String getSenderName();
    String getReceiverName();
    String getSource();
    String getDestination();
    PackageStatus getStatus();
    void setStatus(PackageStatus status);
    String getCurrentStop();
    void advanceToNextStop();
    double calculateBaseCharge();
    void addTrackingRecord(TrackingRecord record);
    boolean waitingForChainedRoute();
}