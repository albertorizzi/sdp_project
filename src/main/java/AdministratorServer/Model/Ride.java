package AdministratorServer.Model;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Ride {
    private int IDRide;
    private Position startPosition;
    private Position destinationPosition;
    private int actualDistrict;

    public Ride(int IDRide, Position startPosition, Position destinationPosition) {
        this.IDRide = IDRide;
        this.startPosition = startPosition;
        this.destinationPosition = destinationPosition;
        this.actualDistrict = startPosition.getDistrictByPosition();
    }

    public Integer getIDRide() {
        return IDRide;
    }

    public void setIDRide(Integer IDRide) {
        this.IDRide = IDRide;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    public Position getDestinationPosition() {
        return destinationPosition;
    }

    public void setDestinationPosition(Position destinationPosition) {
        this.destinationPosition = destinationPosition;
    }

    public int getActualDistrict() {
        return actualDistrict;
    }

    public void setActualDistrict(int actualDistrict) {
        this.actualDistrict = actualDistrict;
    }

    // method to log Ride
    @Override
    public String toString() {
        return "Ride{" +
                "IDRide=" + IDRide +
                ", startPosition=" + startPosition +
                ", destinationPosition=" + destinationPosition +
                '}';
    }

    public String toJsonString() {
        return "{\"ride\":{\"id\":\"" + IDRide + "\",\"" +
                "startPosition\":{\"x\":\"" + startPosition.getX() + "\",\"y\":\"" + startPosition.getY() + "\"}," +
                "\"destinationPosition\":{\"x\":\"" + destinationPosition.getX() + "\",\"y\":\"" + destinationPosition.getY() + "\"}}}";
    }
}
