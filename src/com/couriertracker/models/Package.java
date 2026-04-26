package com.couriertracker.models;

import com.couriertracker.enums.PackageStatus;

import java.util.ArrayList;
import java.util.UUID;

public class Package implements PackageType{
    private String packageID;
    private String senderName;
    private String receiverName;
    private String source;
    private String destination;
    private PackageStatus status;
    private Route route;
    private int currentStopIndex;
    private ArrayList<TrackingRecord> trackingRecords;
    private DeliveryAgent currentAgent;
    private boolean isChained;
    private Route chainedRoute;
    private boolean waitingForChainedRoute;
    private String handoffHub;
    private int hubArrivalStep;
    private int lostAtStep;
    private String lastKnownHubCity;

    public Package(Customer customer, String receiverName, String destination) {
        this.packageID = UUID.randomUUID().toString();
        this.senderName = customer.getName();
        this.receiverName = receiverName;
        this.source = customer.getCity();
        this.destination = destination;
        this.status = PackageStatus.REGISTERED;
        this.route = null;
        this.currentStopIndex = 0;
        this.trackingRecords = new ArrayList<>();
        this.currentAgent = null;
        this.isChained = false;
        this.chainedRoute = null;
        this.waitingForChainedRoute = false;
        this.handoffHub = null;
        this.hubArrivalStep = -1;
        this.lostAtStep = -1;
        this.lastKnownHubCity = null;
    }

    public Package(String senderName, String source, String receiverName, String destination) {
        this.packageID = UUID.randomUUID().toString();
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.source = source;
        this.destination = destination;
        this.status = PackageStatus.REGISTERED;
        this.route = null;
        this.currentStopIndex = 0;
        this.trackingRecords = new ArrayList<>();
        this.currentAgent = null;
        this.isChained = false;
        this.chainedRoute = null;
        this.waitingForChainedRoute = false;
        this.handoffHub = null;
        this.hubArrivalStep = -1;
        this.lostAtStep = -1;
        this.lastKnownHubCity = null;
    }

    public void advanceToNextStop() { // useless? i think we don't need this
        currentStopIndex++;
    }

    public boolean waitingForChainedRoute() {
        return waitingForChainedRoute;
    }

    public double calculateBaseCharge() {
        return 50.0;
    }

    public String getPackageID() {
        return packageID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public Route getRoute() {
        return route;
    }

    public int getCurrentStopIndex() {
        return currentStopIndex;
    }

    public ArrayList<TrackingRecord> getTrackingRecords() {
        return trackingRecords;
    }

    public DeliveryAgent getCurrentAgent() {
        return currentAgent;
    }

    public boolean isChained() {
        return isChained;
    }

    public Route getChainedRoute() {
        return chainedRoute;
    }

    public String getHandoffHub() {
        return handoffHub;
    }

    public int getHubArrivalStep() {
        return hubArrivalStep;
    }

    public int getLostAtStep() {
        return lostAtStep;
    }

    public String getLastKnownHubCity() {
        return lastKnownHubCity;
    }

    public String getCurrentStop() {
        if (route == null) {
            return null;
        }
        if (currentStopIndex < 0 || currentStopIndex >= route.getStops().length) {
            return null;
        }
        return route.getCurrentStop(currentStopIndex);
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setCurrentStopIndex(int currentStopIndex) {
        this.currentStopIndex = currentStopIndex;
    }

    public void setCurrentAgent(DeliveryAgent currentAgent) {
        this.currentAgent = currentAgent;
    }

    public void setChained(boolean chained) {
        isChained = chained;
    }

    public void setChainedRoute(Route chainedRoute) {
        this.chainedRoute = chainedRoute;
    }

    public void setWaitingForChainedRoute(boolean waitingForChainedRoute) {
        this.waitingForChainedRoute = waitingForChainedRoute;
    }

    public void setHandoffHub(String handoffHub) {
        this.handoffHub = handoffHub;
    }

    public void setHubArrivalStep(int hubArrivalStep) {
        this.hubArrivalStep = hubArrivalStep;
    }

    public void setLostAtStep(int lostAtStep) {
        this.lostAtStep = lostAtStep;
    }

    public void setLastKnownHubCity(String lastKnownHubCity) {
        this.lastKnownHubCity = lastKnownHubCity;
    }

    public void addTrackingRecord(TrackingRecord record) {
        trackingRecords.add(record);
    }
}
