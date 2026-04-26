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

    public Package getPackageById(String packageId){
        // check active packages
        for(Package p : activePackages){
            if(p.getPackageID().equals(packageId)){
                return p;
            }
        }
        // check lost packages
        for(Package p : lostPackages){
            if(p.getPackageID().equals(packageId)){
                return p;
            }
        }
        // check delivered packages
        for(Package p : deliveredPackages){
            if(p.getPackageID().equals(packageId)){
                return p;
            }
        }
        return null;
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
        ArrayList<Route> sourceRoutes = new ArrayList<>();
        ArrayList<Route> destinationRoutes = new ArrayList<>();
        // First pass: single route search covering both source and destination
        for(Route r : routes){
            boolean coversSource = false;
            boolean coversDestination = false;
            for(String stop : r.getStops()){
                if(stop.equals(pkg.getSource())){
                    coversSource = true;
                }
                if(stop.equals(pkg.getDestination())){
                    coversDestination = true;
                }
            }
            if(coversSource){
                sourceRoutes.add(r);
            }
            if(coversDestination){
                destinationRoutes.add(r);
            }
            if(coversSource && coversDestination){
                pkg.setRoute(r);
                pkg.setCurrentStopIndex(r.getStopIndex(pkg.getSource()));
                pkg.setLastKnownHubCity(pkg.getSource());
                pkg.setChained(false);
                return true;
            }

        }
        // Second pass: find chained route pair that covers both source and destination and shares common hub
        if(sourceRoutes.size() > 0 && destinationRoutes.size() > 0){
            for(Route s : sourceRoutes){
                for(Route d : destinationRoutes){
                    for(String stop : s.getStops()){
                        for(String dStop : d.getStops()){
                            if(dStop.equals(stop) && !stop.equals(pkg.getSource())){
                                pkg.setRoute(s);
                                pkg.setChained(true);
                                pkg.setChainedRoute(d);
                                pkg.setHandoffHub(stop);
                                pkg.setWaitingForChainedRoute(false);
                                pkg.setCurrentStopIndex(s.getStopIndex(pkg.getSource()));
                                pkg.setLastKnownHubCity(pkg.getSource());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
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

    public boolean handleArrival(DeliveryAgent agent) {
        systemStepCount++;

        agent.arriveAtHub();
        
        checkDelayedPackages();

        boolean finished = ((agent.getCurrentStopIndex() == agent.getActiveRoute().getStops().length - 1 && !agent.isReverseDirection()) || (agent.getCurrentStopIndex() == 0 && agent.isReverseDirection()));
        if(finished){
            agent.reDepositUndeliveredPackages();
            agent.setAvailable(true);
            agent.setActiveRoute(null);
        }

        return finished;
        
        
    }


    public void handleConfirmPickups(DeliveryAgent agent) {
        agent.pickUpPackages();
        ArrayList<Package> droppedPackages = agent.dropOffPackages();
        for(Package pkg : droppedPackages){
            inventoryFileHandler.logEvent(pkg.getTrackingRecords().get(pkg.getTrackingRecords().size() - 1));
            if(pkg.getStatus() == PackageStatus.DELIVERED){
                markDelivered(pkg);
            }
        }
        for(Package pkg : droppedPackages){
            if(pkg.getStatus() == PackageStatus.IN_WAREHOUSE){
                pkg.setHubArrivalStep(systemStepCount);
            }
        }
        for(Package pkg : agent.getCarriedPackages()){
            inventoryFileHandler.logEvent(pkg.getTrackingRecords().get(pkg.getTrackingRecords().size() - 1));
        }
    }

    public void checkDelayedPackages() {
        // scannign all route warehouses for delayed packages
        for(Route route : routes){
            for(int i = 0; i < route.getStops().length; i++){
                ArrayList<Package> pkgs = new ArrayList<>(route.getPackagesAtStop(i));
                for(Package pkg : pkgs){
                    if(pkg.getHubArrivalStep() != -1 && (systemStepCount - pkg.getHubArrivalStep()) >= DELAY_THRESHOLD) {
                        pkg.setStatus(PackageStatus.DELAYED);
                        TrackingRecord tr = new TrackingRecord(pkg.getPackageID(), pkg.getLastKnownHubCity(), PackageStatus.DELAYED, null);
                        pkg.addTrackingRecord(tr);
                        inventoryFileHandler.logEvent(tr);
                    }
                }
            }
        }
        // check lost packages for termination
        ArrayList<Package> lostCopy = new ArrayList<>(lostPackages);
        for(Package pkg : lostCopy){
            if((systemStepCount - pkg.getLostAtStep()) >= 5){
                terminatePackage(pkg);
            }
        }
    }

    public boolean agentSelectsRoute(DeliveryAgent agent) {
        if (agent == null) {
            return false;
        }
        return agent.selectNextRoute();
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
