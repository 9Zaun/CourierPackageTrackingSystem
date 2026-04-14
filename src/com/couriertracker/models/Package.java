import java.util.ArrayList;
import java.util.UUID;

public class Package {
    private String packageID;
    private String senderName;
    private String receiverName;
    private String source;
    private String destination;
    private PackageStatus status;
    private Route route;
    private int currentStopIndex;
    private ArrayList<TrackingRecord> trackingRecords;

    public Package(Customer customer, String receiverName, String destination) {
        this.packageID = UUID.randomUUID().toString();
        this.senderName = customer.getName();
        this.receiverName = receiverName;
        this.source = customer.getAddress();
        this.destination = destination;
        this.status = PackageStatus.REGISTERED;
        this.route = inventory.assignRoute(source, destination);
        this.currentStopIndex = 0;
        this.trackingRecords = new ArrayList<>();
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
    
    
    public TrackingRecord[] getTrackingRecords() {
        return trackingRecords;
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

    public void setRoute(Route route) {
        this.route = route;
    }
    
    public void setCurrentStopIndex(int currentStopIndex) {
        this.currentStopIndex = currentStopIndex;
    }

    public void addTrackingRecord(TrackingRecord record) {
        trackingRecords[currentStopIndex] = record;

    }

    public void advanceToNextStop() {
        currentStopIndex++;
    }

    public double calculateBaseCharge(){

    }

    public void updateStatus(PackageStatus newStatus) {
        this.status = newStatus;
    }

    public void viewPackageStatus(){
        System.out.println("Package ID: " + packageID);
        System.out.println("Sender Name: " + senderName);
        System.out.println("Receiver Name: " + receiverName);
        System.out.println("Source: " + source);
        System.out.println("Destination: " + destination);
        System.out.println("Last hub visited: " + route.getCurrentStop(currentStopIndex));
        System.out.println("Status: " + status);
        System.out.println("Route: " + route.displayRoute());
    }
    
    
}