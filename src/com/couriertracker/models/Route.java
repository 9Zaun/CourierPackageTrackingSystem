import java.util.UUID;
public class Route{
    private String routeID;
    private String source;
    private String destination;
    private String[] stops;
    private DeliveryAgents[] assignedAgents;

    public Route(String source, destination, String[] stops, DeliveryAgent[] assignedAgents){
        this.routeID = UUID.randomUUID().toString();
        this.source = source;
        this.destination = destination;
        this.stops = stops;
        this.assignedAgents = assignedAgents;
    }

    public String getCurrentStop(int index){
        return stops[index];
    }

    public String getNextStop(int index){
        return stops[index + 1];
    }

    public int getNumberOfPaths(){
        return stops.length - 1;
    }

    public DeliveryAgent getAgentForPath(int pathIndex){
        return assignedAgents[pathIndex];
    }

    public String displayRoute(){
        String route = "";
        for(int i = 0; i < stops.length - 1; i++){
            route += stops[i] + " -> ";
        }
        route += stops[stops.length - 1];
        return route;
    }
}