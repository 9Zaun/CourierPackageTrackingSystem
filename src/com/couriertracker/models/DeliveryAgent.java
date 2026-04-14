import java.util.UUID;

public class DeliveryAgent{
    private String agentID;
    private String name;
    private String currentLocation;
    private boolean isAvailable;

    public DeliveryAgent(String name){
        this.agentID = UUID.randomUUID().toString();
        this.name = name;
        this.currentLocation = "None";
        this.isAvailable = true;
    }

    public void pickUpPackage(Package pkg){
        pkg.updateStatus(PackageStatus.PICKED_UP);
    }

    public void transportingPackage(Package pkg){
        pkg.updateStatus(PackageStatus.IN_TRANSIT);
    }

    public void moveToNextHub(Package pkg){
        pkg.advanceToNextStop();
        pkg.updateStatus(PackageStatus.AT_HUB);
    }

    public void handoffPackage(Package pkg){
        
    }
}