package com.couriertracker.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Route {
    private String routeID;
    private String source;
    private String destination;
    private String[] stops;
    private HashMap<Integer, ArrayList<Package>> warehouse;
    private int warehousePackageCount;

    public Route(String source, String destination, String[] stops) {
        this.routeID = UUID.randomUUID().toString();
        this.source = source;
        this.destination = destination;
        this.stops = stops;
        this.warehouse = new HashMap<>();
        for (int i = 0; i < stops.length; i++) {
            this.warehouse.put(i, new ArrayList<>());
        }
        this.warehousePackageCount = 0;
    }

    public int getLiveWarehousePackageCount() {
        int count = 0;
        for (ArrayList<Package> list : warehouse.values()) {
            count += list.size();
        }
        return count;
    }

    public String getRouteID() {
        return routeID;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String[] getStops() {
        return stops;
    }

    public int getWarehousePackageCount() {
        return warehousePackageCount;
    }

    public String getCurrentStop(int index) {
        return stops[index];
    }

    public boolean containsStop(String city) {
        for (String stop : stops) {
            if (stop != null && stop.equals(city)) {
                return true;
            }
        }
        return false;
    }

    public int getStopIndex(String city) {
        for (int i = 0; i < stops.length; i++) {
            if (stops[i] != null && stops[i].equals(city)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Package> getPackagesAtStop(int stopIndex) {
        return warehouse.getOrDefault(stopIndex, new ArrayList<>());
    }

    public void addPackageToWarehouse(int stopIndex, Package pkg) {
        ArrayList<Package> list = warehouse.get(stopIndex);
        if (list == null) {
            list = new ArrayList<>();
            warehouse.put(stopIndex, list);
        }
        list.add(pkg);
        warehousePackageCount++;
    }

    public boolean removePackageFromWarehouse(int stopIndex, Package pkg) {
        ArrayList<Package> list = warehouse.get(stopIndex);
        if (list == null) {
            return false;
        }
        boolean removed = list.remove(pkg);
        if (removed) {
            warehousePackageCount--;
        }
        return removed;
    }

    public void clearStop(int stopIndex) {
        ArrayList<Package> list = warehouse.get(stopIndex);
        if (list == null || list.isEmpty()) {
            return;
        }
        int n = list.size();
        list.clear();
        warehousePackageCount -= n;
    }

    public String displayRoute() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stops.length - 1; i++) {
            sb.append(stops[i]).append(" -> ");
        }
        if (stops.length > 0) {
            sb.append(stops[stops.length - 1]);
        }
        return sb.toString();
    }
}
