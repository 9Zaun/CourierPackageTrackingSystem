package com.couriertracker.models;

import com.couriertracker.enums.PackageStatus;

import java.util.ArrayList;
import java.util.UUID;

public class DeliveryAgent {
    private String agentID;
    private String name;
    private String currentLocation;
    private boolean available;
    private ArrayList<Route> ownedRoutes;
    private Route activeRoute;
    private int currentStopIndex;
    private boolean reverseDirection;
    private ArrayList<Package> carriedPackages;

    public DeliveryAgent(String name) {
        this.agentID = UUID.randomUUID().toString();
        this.name = name;
        this.currentLocation = "None";
        this.available = true;
        this.ownedRoutes = new ArrayList<>();
        this.activeRoute = null;
        this.currentStopIndex = 0;
        this.reverseDirection = false;
        this.carriedPackages = new ArrayList<>();
    }

    public void addOwnedRoute(Route route) {
        if (route != null && ownedRoutes.size() < 3 && !ownedRoutes.contains(route)) {
            ownedRoutes.add(route);
        }
    }

    public void pickUpPackages() {
        if(activeRoute == null){
            return;
        }
        ArrayList<Package> atStop = new ArrayList<>(activeRoute.getPackagesAtStop(currentStopIndex));
        for (Package pkg : atStop) {

            if(!pkg.isChained()){
                pkg.setStatus(PackageStatus.PICKED_UP);
                pkg.setCurrentAgent(this);
                pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                        PackageStatus.PICKED_UP, this));
                pkg.setHubArrivalStep(-1);
                carriedPackages.add(pkg);
            } else {
                Route expectedRoute = pkg.waitingForChainedRoute() ? pkg.getChainedRoute() : pkg.getRoute();
                if(expectedRoute.getRouteID().equals(activeRoute.getRouteID())){
                    pkg.setStatus(PackageStatus.PICKED_UP);
                    pkg.setCurrentAgent(this);
                    pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                            PackageStatus.PICKED_UP, this));
                    pkg.setHubArrivalStep(-1);
                    carriedPackages.add(pkg);
                    
                }
            }
        }
        activeRoute.clearStop(currentStopIndex);
    }

    public void departHub() {
        String departureCity = currentLocation;
        for (Package pkg : new ArrayList<>(carriedPackages)) {
            pkg.setStatus(PackageStatus.IN_TRANSIT);
            pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), departureCity,
                    PackageStatus.IN_TRANSIT, this));
        }
    }

    public boolean selectNextRoute() {
        int maxCount = 0;
        Route nextRoute = null;
        for(Route route : ownedRoutes){ // find route with greatest demand 
            int liveCount = route.getLiveWarehousePackageCount();
            if (liveCount > maxCount) {
                maxCount = liveCount;
                nextRoute = route;
            }
        }
        if(nextRoute == null){ // if all routes have 0 packages, do nothing (agent tries again later)
            return false;
        }
        if(nextRoute.getRouteID().equals(activeRoute != null ? activeRoute.getRouteID() : "")){  // if greatest demand route is same as active route, reverse direction instead of finding side with greater demand
            reverseDirection = !reverseDirection;
            activeRoute = nextRoute;
            available = false;
            currentLocation = reverseDirection ? nextRoute.getStops()[nextRoute.getStops().length - 1] : nextRoute.getStops()[0];
            currentStopIndex = reverseDirection ? nextRoute.getStops().length - 1 : 0;
            return true;
        }

        int first_half = 0; int second_half = 0;
        for(int i = 0; i < nextRoute.getStops().length/2; i++){ // find side with greater demand 
            first_half += nextRoute.getPackagesAtStop(i).size();
            second_half += nextRoute.getPackagesAtStop(nextRoute.getStops().length - i - 1).size();
        }
        if(first_half > second_half){
            activeRoute = nextRoute;
            reverseDirection = false;
            currentStopIndex = 0;
            currentLocation = nextRoute.getStops()[0];
            available = false;
            return true;
        } else {
            activeRoute = nextRoute;
            reverseDirection = true;
            currentStopIndex = nextRoute.getStops().length - 1;
            currentLocation = nextRoute.getStops()[nextRoute.getStops().length - 1];
            available = false;
            return true;
        }
    }

    public void arriveAtHub() {
        if(reverseDirection){
            currentStopIndex--;
        } else {
            currentStopIndex++;
        }
        currentLocation = activeRoute.getStops()[currentStopIndex];
        for(Package pkg : carriedPackages){
            pkg.setStatus(PackageStatus.AT_HUB);
            pkg.setCurrentStopIndex(currentStopIndex);
            pkg.setLastKnownHubCity(currentLocation);
            pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                    PackageStatus.AT_HUB, this));
        }

    }

    public ArrayList<Package> dropOffPackages() {
        if(activeRoute == null){
            return new ArrayList<>();
        }
        ArrayList<Package> dropOff = new ArrayList<>(carriedPackages);
        ArrayList<Package> deliveredPackages = new ArrayList<>();
        for(Package pkg : dropOff){
            if(!pkg.isChained()){
                if(pkg.getDestination().equals(currentLocation)){
                    pkg.setStatus(PackageStatus.DELIVERED);
                    pkg.setLastKnownHubCity(currentLocation);
                    pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                            PackageStatus.DELIVERED, this));
                    pkg.setCurrentAgent(null);
                    carriedPackages.remove(pkg);
                    deliveredPackages.add(pkg);
                }
            } else {
                if(!pkg.waitingForChainedRoute()){
                    if(pkg.getHandoffHub().equals(currentLocation)){
                        pkg.setStatus(PackageStatus.IN_WAREHOUSE);
                        pkg.setLastKnownHubCity(currentLocation);
                        pkg.setWaitingForChainedRoute(true);
                        pkg.setRoute(pkg.getChainedRoute());
                        pkg.getChainedRoute().addPackageToWarehouse(pkg.getChainedRoute().getStopIndex(currentLocation), pkg);
                        pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                                PackageStatus.IN_WAREHOUSE, this));
                        pkg.setCurrentAgent(null);
                        carriedPackages.remove(pkg);
                        deliveredPackages.add(pkg);
                    }
                } else {
                    if(pkg.getDestination().equals(currentLocation)){
                        pkg.setStatus(PackageStatus.DELIVERED);
                        pkg.setLastKnownHubCity(currentLocation);
                        pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                                PackageStatus.DELIVERED, this));
                        pkg.setCurrentAgent(null);
                        carriedPackages.remove(pkg);
                        deliveredPackages.add(pkg);
                    }
                }

            }

        }
        return deliveredPackages;
    }

    public void reDepositUndeliveredPackages() {
        if (activeRoute == null) {
            return;
        }
        for (Package pkg : new ArrayList<>(carriedPackages)) {
            if (!pkg.isChained() && !pkg.getDestination().equals(currentLocation)) {
                activeRoute.addPackageToWarehouse(currentStopIndex, pkg);
                pkg.setStatus(PackageStatus.IN_WAREHOUSE);
                pkg.setLastKnownHubCity(currentLocation);
                pkg.setCurrentStopIndex(currentStopIndex);
                pkg.setHubArrivalStep(-1);
                pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation, PackageStatus.IN_WAREHOUSE, this));
                pkg.setCurrentAgent(null);
                carriedPackages.remove(pkg);
            }
        }
    }

    public String getAgentID() {
        return agentID;
    }

    public String getName() {
        return name;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public boolean isAvailable() {
        return available;
    }

    public ArrayList<Route> getOwnedRoutes() {
        return ownedRoutes;
    }

    public Route getActiveRoute() {
        return activeRoute;
    }

    public int getCurrentStopIndex() {
        return currentStopIndex;
    }

    public boolean isReverseDirection() {
        return reverseDirection;
    }

    public ArrayList<Package> getCarriedPackages() {
        return carriedPackages;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setActiveRoute(Route activeRoute) {
        this.activeRoute = activeRoute;
    }

    public void setCurrentStopIndex(int currentStopIndex) {
        this.currentStopIndex = currentStopIndex;
    }

    public void setReverseDirection(boolean reverseDirection) {
        this.reverseDirection = reverseDirection;
    }
}
