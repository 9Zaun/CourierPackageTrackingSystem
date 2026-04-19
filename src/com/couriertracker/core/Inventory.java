package com.couriertracker.core;

import com.couriertracker.enums.PackageStatus;
import com.couriertracker.handlers.InventoryFileHandler;
import com.couriertracker.handlers.TransactionFileHandler;
import com.couriertracker.models.Customer;
import com.couriertracker.models.DeliveryAgent;
import com.couriertracker.models.Discount;
import com.couriertracker.models.Package;
import com.couriertracker.models.Route;
import com.couriertracker.models.TrackingRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Inventory {
    public static final int DELAY_THRESHOLD = 5;

    private final ArrayList<DeliveryAgent> agents;
    private final ArrayList<Route> routes;
    private final ArrayList<Package> activePackages;
    private final ArrayList<Package> deliveredPackages;
    private final ArrayList<Customer> customers;
    private final ArrayList<Package> lostPackages;
    private final ArrayList<LostTicket> lostTickets;
    private final ArrayList<Package> terminatedPackages;
    private final InventoryFileHandler inventoryFileHandler;
    private final TransactionFileHandler transactionFileHandler;
    private int systemStepCount;

    public Inventory() {
        this.agents = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.activePackages = new ArrayList<>();
        this.deliveredPackages = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.lostPackages = new ArrayList<>();
        this.lostTickets = new ArrayList<>();
        this.terminatedPackages = new ArrayList<>();
        this.inventoryFileHandler = new InventoryFileHandler();
        this.transactionFileHandler = new TransactionFileHandler();
        this.systemStepCount = 0;
    }

    public void addCustomerIfAbsent(Customer customer) {
        if (customer == null) {
            return;
        }
        for (Customer c : customers) {
            if (c.getCustomerID().equals(customer.getCustomerID())) {
                return;
            }
        }
        customers.add(customer);
    }

    public void registerPackage(Package pkg, Customer customer) {
        if (!attemptRouteAssignment(pkg)) {
            pkg.setStatus(PackageStatus.RETURNED);
            TrackingRecord tr = new TrackingRecord(pkg.getPackageID(), pkg.getSource(), PackageStatus.RETURNED, null);
            pkg.addTrackingRecord(tr);
            inventoryFileHandler.logEvent(tr);
            transactionFileHandler.logPayment(pkg.getPackageID(), customer != null ? customer.getName() : "",
                    pkg.getDestination(), 0, 0, 0);
            return;
        }

        Route assigned = pkg.getRoute();
        int sourceIdx = assigned.getStopIndex(pkg.getSource());
        assigned.addPackageToWarehouse(sourceIdx, pkg);
        pkg.setHubArrivalStep(systemStepCount);

        TrackingRecord registered = new TrackingRecord(pkg.getPackageID(), pkg.getSource(),
                PackageStatus.REGISTERED, null);
        pkg.addTrackingRecord(registered);
        inventoryFileHandler.logEvent(registered);

        activePackages.add(pkg);

        double base = pkg.calculateBaseCharge();
        double finalAmount = base;
        double discountApplied = 0;
        if (customer != null && customer.getDiscount() != null) {
            Discount d = customer.getDiscount();
            double after = d.applyDiscount(base);
            discountApplied = base - after;
            finalAmount = after;
        }
        transactionFileHandler.logPayment(pkg.getPackageID(), customer != null ? customer.getName() : "",
                pkg.getDestination(), base, discountApplied, finalAmount);
    }

    private boolean attemptRouteAssignment(Package pkg) {
        // TODO: implement manually
        return false;
    }

    public void handleConfirmPickups(DeliveryAgent agent) {
        if (agent == null || agent.getActiveRoute() == null) {
            return;
        }
        agent.pickUpPackages(agent.getActiveRoute(), agent.getCurrentStopIndex());
    }

    public void handleStartTravelling(DeliveryAgent agent) {
        if (agent == null) {
            return;
        }
        agent.departHub();
        for (Package pkg : agent.getCarriedPackages()) {
            ArrayList<TrackingRecord> records = pkg.getTrackingRecords();
            if (!records.isEmpty()) {
                TrackingRecord last = records.get(records.size() - 1);
                inventoryFileHandler.logEvent(last);
            }
        }
    }

    public void markDelivered(Package pkg) {
        if (pkg == null) {
            return;
        }
        transactionFileHandler.logDeliveryConfirmed(pkg.getPackageID(), pkg.getSenderName(), pkg.getDestination());
        activePackages.remove(pkg);
        deliveredPackages.add(pkg);
    }

    public void handleLostPackage(Package pkg, DeliveryAgent agent) {
        if (pkg == null || agent == null) {
            return;
        }
        Route route = agent.getActiveRoute();
        int stopIndex = agent.getCurrentStopIndex();
        if (route != null) {
            route.removePackageFromWarehouse(stopIndex, pkg);
        }
        pkg.setStatus(PackageStatus.LOST);
        pkg.setLostAtStep(systemStepCount);
        pkg.setLastKnownHubCity(agent.getCurrentLocation());
        TrackingRecord tr = new TrackingRecord(pkg.getPackageID(), agent.getCurrentLocation(),
                PackageStatus.LOST, agent);
        pkg.addTrackingRecord(tr);
        inventoryFileHandler.logEvent(tr);

        lostPackages.add(pkg);
        LostTicket ticket = new LostTicket(
                UUID.randomUUID().toString(),
                pkg.getPackageID(),
                route != null ? route.getRouteID() : "",
                agent.getCurrentLocation(),
                systemStepCount);
        lostTickets.add(ticket);
    }

    public void resolveLostPackage(Package pkg) {
        if (pkg == null || !lostPackages.contains(pkg)) {
            return;
        }
        Route route = pkg.getRoute();
        if (route == null) {
            return;
        }
        String city = pkg.getLastKnownHubCity();
        int stopIdx = route.getStopIndex(city);
        if (stopIdx < 0) {
            stopIdx = pkg.getCurrentStopIndex();
        }
        route.addPackageToWarehouse(stopIdx, pkg);
        pkg.setStatus(PackageStatus.IN_WAREHOUSE);
        TrackingRecord tr = new TrackingRecord(pkg.getPackageID(), city, PackageStatus.IN_WAREHOUSE, null);
        pkg.addTrackingRecord(tr);
        pkg.setHubArrivalStep(systemStepCount);
        inventoryFileHandler.logEvent(tr);

        lostPackages.remove(pkg);
        lostTickets.removeIf(t -> t.getPackageId().equals(pkg.getPackageID()));
    }

    public void terminatePackage(Package pkg) {
        if (pkg == null || !lostPackages.contains(pkg)) {
            return;
        }
        pkg.setStatus(PackageStatus.LOST);
        TrackingRecord tr = new TrackingRecord(pkg.getPackageID(),
                pkg.getLastKnownHubCity() != null ? pkg.getLastKnownHubCity() : pkg.getSource(),
                PackageStatus.LOST, null);
        pkg.addTrackingRecord(tr);
        inventoryFileHandler.logEvent(tr);

        double refund = pkg.calculateBaseCharge();
        transactionFileHandler.logRefund(pkg.getPackageID(), pkg.getSenderName(), refund);

        lostPackages.remove(pkg);
        lostTickets.removeIf(t -> t.getPackageId().equals(pkg.getPackageID()));
        terminatedPackages.add(pkg);
    }

    public void handleArrival(DeliveryAgent agent) {
        // TODO: implement manually
    }

    public void checkDelayedPackages() {
        // TODO: implement manually
    }

    public void agentSelectsRoute(DeliveryAgent agent) {
        if (agent == null) {
            return;
        }
        agent.selectNextRoute();
    }

    public Package trackPackage(String packageId) {
        if (packageId == null) {
            return null;
        }
        for (Package p : activePackages) {
            if (packageId.equals(p.getPackageID())) {
                return p;
            }
        }
        for (Package p : deliveredPackages) {
            if (packageId.equals(p.getPackageID())) {
                return p;
            }
        }
        return null;
    }

    public DeliveryAgent getAgentByName(String name) {
        if (name == null) {
            return null;
        }
        for (DeliveryAgent a : agents) {
            if (name.equals(a.getName())) {
                return a;
            }
        }
        return null;
    }

    public void addAgent(DeliveryAgent agent) {
        if (agent != null) {
            agents.add(agent);
        }
    }

    public void addRoute(Route route) {
        if (route != null) {
            routes.add(route);
        }
    }

    public int getSystemStepCount() {
        return systemStepCount;
    }

    public List<DeliveryAgent> getAgents() {
        return Collections.unmodifiableList(new ArrayList<>(agents));
    }

    public List<Route> getRoutes() {
        return Collections.unmodifiableList(new ArrayList<>(routes));
    }

    public List<Package> getActivePackages() {
        return Collections.unmodifiableList(new ArrayList<>(activePackages));
    }

    public List<Package> getDeliveredPackages() {
        return Collections.unmodifiableList(new ArrayList<>(deliveredPackages));
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers));
    }

    public List<Package> getLostPackages() {
        return Collections.unmodifiableList(new ArrayList<>(lostPackages));
    }

    public List<LostTicket> getLostTickets() {
        return Collections.unmodifiableList(new ArrayList<>(lostTickets));
    }

    public List<Package> getTerminatedPackages() {
        return Collections.unmodifiableList(new ArrayList<>(terminatedPackages));
    }

    public InventoryFileHandler getInventoryFileHandler() {
        return inventoryFileHandler;
    }

    public TransactionFileHandler getTransactionFileHandler() {
        return transactionFileHandler;
    }

    public static final class LostTicket {
        private final String ticketId;
        private final String packageId;
        private final String routeId;
        private final String lastKnownCity;
        private final int lostAtStep;

        public LostTicket(String ticketId, String packageId, String routeId, String lastKnownCity, int lostAtStep) {
            this.ticketId = ticketId;
            this.packageId = packageId;
            this.routeId = routeId;
            this.lastKnownCity = lastKnownCity;
            this.lostAtStep = lostAtStep;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getPackageId() {
            return packageId;
        }

        public String getRouteId() {
            return routeId;
        }

        public String getLastKnownCity() {
            return lastKnownCity;
        }

        public int getLostAtStep() {
            return lostAtStep;
        }
    }
}
