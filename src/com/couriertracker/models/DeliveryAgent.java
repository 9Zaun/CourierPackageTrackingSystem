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
        if(route == null){
            return;
        }
        ArrayList<Package> atStop = new ArrayList<>(route.getPackagesAtStop(currentStopIndex));
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
                if(expectedRoute.getRouteID() == activeRoute.getRouteID()){
                    pkg.setStatus(PackageStatus.PICKED_UP);
                    pkg.setCurrentAgent(this);
                    pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), currentLocation,
                            PackageStatus.PICKED_UP, this));
                    pkg.setHubArrivalStep(-1);
                    carriedPackages.add(pkg);
                    if(pkg.waitingForChainedRoute()){
                        pkg.setWaitingForChainedRoute(false);
                        pkg.setChainedRoute(null);
                    }
                }
            }
        }
        route.clearStop(currentStopIndex);
    }

    public void departHub() {
        String departureCity = currentLocation;
        for (Package pkg : new ArrayList<>(carriedPackages)) {
            pkg.setStatus(PackageStatus.IN_TRANSIT);
            pkg.addTrackingRecord(new TrackingRecord(pkg.getPackageID(), departureCity,
                    PackageStatus.IN_TRANSIT, this));
        }
    }

    public void selectNextRoute() {
        maxCount = 0;
        nextRoute = null;
        for(Route route : ownedRoutes){ // find route with greatest demand 
            if(route.getWarehousePackageCount() > maxCount){
                maxCount = route.getWarehousePackageCount();
                nextRoute = route;
            }
        }
        if(nextRoute.routeID == activeRoute.routeID){ // if greatest demand route is same as active route, reverse direction instead of finding side with greater demand
            reverseDirection = !reverseDirection;
            activeRoute = nextRoute;
            available = false;
            currentLocation = reverseDirection ? nextRoute.getStops()[nextRoute.getStops().length - 1] : nextRoute.getStops()[0];
            return;
        }

        int first_half = 0; int second_half = 0;
        for(int i = 0; i < nextRoute.getStops().length/2; i++){ // find side with greater demand 
            first_half += nextRoute.warehouse.get(i).size();
            second_half += nextRoute.warehouse.get(nextRoute.getStops().length - i - 1).size();
        }
        if(first_half > second_half){
            activeRoute = nextRoute;
            reverseDirection = false;
            currentStopIndex = 0;
            currentLocation = nextRoute.getStops()[0];
        } else {
            activeRoute = nextRoute;
            reverseDirection = true;
            currentStopIndex = nextRoute.getStops().length - 1;
            currentLocation = nextRoute.getStops()[nextRoute.getStops().length - 1];
        }
        available = false;
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

    public void dropOffPackages() {
        // TODO: implement manually
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
