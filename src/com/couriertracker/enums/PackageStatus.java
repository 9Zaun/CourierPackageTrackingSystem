package com.couriertracker.enums;

public enum PackageStatus {
    REGISTERED, // package created, payment received, but not picked up
    PICKED_UP, // package picked up from courier office or sender
    IN_TRANSIT, // moving between hubs via delivery agent
    AT_HUB, // arrived at an intermediate stop, waiting for departure to next stop
    IN_WAREHOUSE, // arrived at a stop, got handed off but no agents available; waiting for agent pickup
    DELIVERED, // reached destination
    DELAYED, // mid-transit issues, waiting for delivery agent to resolve
    CANCELLED, // cancelled by sender or receiver
    LOST, // lost in transit, not found
    RETURNED // returned to sender due to address issues
}
