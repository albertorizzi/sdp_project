package AdministratorServer.Model;

public class Ride {
    private Integer IDRide;
    private Position startPosition;
    private Position destinationPosition;
    private int actualDistrict;

    public Ride(Integer IDRide, Position startPosition, Position destinationPosition, int actualDistrict) {
        this.IDRide = IDRide;
        this.startPosition = startPosition;
        this.destinationPosition = destinationPosition;
        this.actualDistrict = actualDistrict;
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
}
