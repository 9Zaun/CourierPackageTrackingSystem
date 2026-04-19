package com.couriertracker.core;

import com.couriertracker.models.Customer;
import com.couriertracker.models.DeliveryAgent;
import com.couriertracker.models.Package;
import com.couriertracker.models.Route;

import java.util.List;

public class CourierService {
    private final String serviceName;
    private final Inventory inventory;

    public CourierService(String serviceName) {
        this.serviceName = serviceName;
        this.inventory = new Inventory();
        setupAgentsAndRoutes();
    }

    public String getServiceName() {
        return serviceName;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void registerShipment(Customer customer, String receiverName, String destinationCity) {
        Package pkg = new Package(customer, receiverName, destinationCity);
        inventory.addCustomerIfAbsent(customer);
        inventory.registerPackage(pkg, customer);
    }

    public Package createPackage(Customer customer, String receiverName, String destinationCity) {
        return new Package(customer, receiverName, destinationCity);
    }

    public void registerPackage(Package pkg, Customer customer) {
        inventory.addCustomerIfAbsent(customer);
        inventory.registerPackage(pkg, customer);
    }

    public Package trackPackage(String packageId) {
        return inventory.trackPackage(packageId);
    }

    public void confirmPickups(DeliveryAgent agent) {
        inventory.handleConfirmPickups(agent);
    }

    public void startTravelling(DeliveryAgent agent) {
        inventory.handleStartTravelling(agent);
    }

    public void agentArrived(DeliveryAgent agent) {
        inventory.handleArrival(agent);
    }

    public void agentSelectsRoute(DeliveryAgent agent) {
        inventory.agentSelectsRoute(agent);
    }

    public void markDelivered(Package pkg) {
        inventory.markDelivered(pkg);
    }

    public void handleLostPackage(Package pkg, DeliveryAgent agent) {
        inventory.handleLostPackage(pkg, agent);
    }

    public void resolveLostPackage(Package pkg) {
        inventory.resolveLostPackage(pkg);
    }

    public void terminatePackage(Package pkg) {
        inventory.terminatePackage(pkg);
    }

    public DeliveryAgent getAgentByName(String name) {
        return inventory.getAgentByName(name);
    }

    public List<DeliveryAgent> getAgents() {
        return inventory.getAgents();
    }

    public List<Route> getRoutes() {
        return inventory.getRoutes();
    }

    public List<Package> getActivePackages() {
        return inventory.getActivePackages();
    }

    public List<Package> getDeliveredPackages() {
        return inventory.getDeliveredPackages();
    }

    public List<Customer> getCustomers() {
        return inventory.getCustomers();
    }

    public List<Package> getLostPackages() {
        return inventory.getLostPackages();
    }

    public List<Inventory.LostTicket> getLostTickets() {
        return inventory.getLostTickets();
    }

    public List<Package> getTerminatedPackages() {
        return inventory.getTerminatedPackages();
    }

    private void setupAgentsAndRoutes() {
        DeliveryAgent agent1 = new DeliveryAgent("Alfa");
        DeliveryAgent agent2 = new DeliveryAgent("Bravo");
        DeliveryAgent agent3 = new DeliveryAgent("Charlie");
        DeliveryAgent agent4 = new DeliveryAgent("Delta");
        DeliveryAgent agent5 = new DeliveryAgent("Echo");
        DeliveryAgent agent6 = new DeliveryAgent("Foxtrot");
        DeliveryAgent agent7 = new DeliveryAgent("Golf");
        DeliveryAgent agent8 = new DeliveryAgent("Hotel");
        DeliveryAgent agent9 = new DeliveryAgent("Indus");
        DeliveryAgent agent10 = new DeliveryAgent("Juliet");
        DeliveryAgent agent11 = new DeliveryAgent("Kilo");
        DeliveryAgent agent12 = new DeliveryAgent("Lima");

        inventory.addAgent(agent1);
        inventory.addAgent(agent2);
        inventory.addAgent(agent3);
        inventory.addAgent(agent4);
        inventory.addAgent(agent5);
        inventory.addAgent(agent6);
        inventory.addAgent(agent7);
        inventory.addAgent(agent8);
        inventory.addAgent(agent9);
        inventory.addAgent(agent10);
        inventory.addAgent(agent11);
        inventory.addAgent(agent12);

        Route route1 = new Route("Mumbai", "Kolkata",
                new String[]{"Mumbai", "Hyderabad", "Vijayawada", "Kolkata"});
        Route route2 = new Route("Delhi", "Chennai",
                new String[]{"Delhi", "Jaipur", "Hyderabad", "Chennai"});
        Route route3 = new Route("Mumbai", "Delhi",
                new String[]{"Mumbai", "Surat", "Ahmedabad", "Delhi"});
        Route route4 = new Route("Bangalore", "Kolkata",
                new String[]{"Bangalore", "Hyderabad", "Vijayawada", "Bhubaneswar", "Kolkata"});
        Route route5 = new Route("Chennai", "Bangalore",
                new String[]{"Chennai", "Vellore", "Bangalore"});
        Route route6 = new Route("Delhi", "Kolkata",
                new String[]{"Delhi", "Varanasi", "Kolkata"});
        Route route7 = new Route("Mumbai", "Bangalore",
                new String[]{"Mumbai", "Pune", "Hyderabad", "Bangalore"});
        Route route8 = new Route("Hyderabad", "Delhi",
                new String[]{"Hyderabad", "Nagpur", "Bhopal", "Delhi"});

        inventory.addRoute(route1);
        inventory.addRoute(route2);
        inventory.addRoute(route3);
        inventory.addRoute(route4);
        inventory.addRoute(route5);
        inventory.addRoute(route6);
        inventory.addRoute(route7);
        inventory.addRoute(route8);

        agent1.addOwnedRoute(route1);
        agent2.addOwnedRoute(route1);
        agent3.addOwnedRoute(route1);

        agent4.addOwnedRoute(route2);
        agent5.addOwnedRoute(route2);
        agent6.addOwnedRoute(route2);

        agent7.addOwnedRoute(route3);
        agent8.addOwnedRoute(route3);
        agent9.addOwnedRoute(route3);

        agent10.addOwnedRoute(route4);
        agent2.addOwnedRoute(route4);
        agent3.addOwnedRoute(route4);
        agent11.addOwnedRoute(route4);

        agent6.addOwnedRoute(route5);
        agent10.addOwnedRoute(route5);

        agent4.addOwnedRoute(route6);
        agent12.addOwnedRoute(route6);

        agent1.addOwnedRoute(route7);
        agent5.addOwnedRoute(route7);
        agent10.addOwnedRoute(route7);

        agent2.addOwnedRoute(route8);
        agent8.addOwnedRoute(route8);
        agent9.addOwnedRoute(route8);
    }
}
