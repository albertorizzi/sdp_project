package AdministratorServer.Model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Taxi {
    private Integer id;
    private Integer portNumber;
    private String addressServerAdministrator;
    private Integer batteryLevel;
    private Position position;
    private List<Taxi> listTaxiKnowed;
    private boolean inCharge = false;
    //private boolean inElection = false;
    private boolean inRide = false;
   // private boolean inExit = false;

  //  private Object rechargeLock = new Object(); // lock durante recharging
   // private Object rideLock = new Object(); // lock durante ride
 //   private Object electionLock = new Object(); // lock durante election

    // empty constructor
    public Taxi() {
    }

    /*
    INITIALIZATION Taxi
        - batteryLevel = 100
        - randomPosition at Taxi creation
    */
    public Taxi(Integer id, Integer portNumber, String addressServerAdministrator) {
        this.id = id;
        this.portNumber = portNumber;
        this.addressServerAdministrator = addressServerAdministrator;
        this.batteryLevel = 100;
    }

    public Taxi(Integer id, Integer portNumber, String addressServerAdministrator, Integer batteryLevel, Position position) {
        this.id = id;
        this.portNumber = portNumber;
        this.addressServerAdministrator = addressServerAdministrator;
        this.batteryLevel = batteryLevel;
        this.position = position;
    }

    public Integer getId() {
        return id;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public String getAddressServerAdministrator() {
        return addressServerAdministrator;
    }

    public void setAddressServerAdministrator(String addressServerAdministrator) {
        this.addressServerAdministrator = addressServerAdministrator;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isInCharge() {
        return inCharge;
    }

    public void setInCharge(boolean inCharge) {
        this.inCharge = inCharge;
    }

    public boolean isInRide() {
        return inRide;
    }

    public void setInRide(boolean inRide) {
        this.inRide = inRide;
    }



    public List<Taxi> getListTaxiKnowed() {
        return listTaxiKnowed;
    }

    public void setListTaxiKnowed(List<Taxi> listTaxiKnowed) {
        this.listTaxiKnowed = listTaxiKnowed;
    }

    public Position defineInitialPositionOfRechargeStation() {
        List<Position> positionOfRechargeStation = new ArrayList<>();
        positionOfRechargeStation.add(new Position(0, 0));
        positionOfRechargeStation.add(new Position(0, 9));
        positionOfRechargeStation.add(new Position(9, 0));
        positionOfRechargeStation.add(new Position(9, 9));

        int randomIndex = (int) (Math.random() * positionOfRechargeStation.size());
        Position randomPosition = positionOfRechargeStation.get(randomIndex);
        return randomPosition;
    }

    @Override
    public String toString() {
        return "Taxi{" +
                "id=" + id +
                ", portNumber=" + portNumber +
                ", addressServerAdministrator='" + addressServerAdministrator + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", position=" + position +
                ", listTaxiKnowed=" + listTaxiKnowed +
                '}';
    }
}

