package AdministratorServer.Model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Taxis {
    @XmlElement(name = "taxis")
    private final List<Taxi> taxislist;

    private static Taxis instance;

    // constructor
    private Taxis() {
        taxislist = new ArrayList<Taxi>();
    }

    //singleton
    public synchronized static Taxis getInstance() {
        if (instance == null)
            instance = new Taxis();
        return instance;
    }

    public synchronized List<Taxi> getTaxisList() {
        return new ArrayList<>(taxislist);
    }

    public synchronized void add(Taxi taxi) {
        Position position = taxi.defineInitialPositionOfRechargeStation();
        taxi.setPosition(position);
        taxi.setBatteryLevel(100);
        taxislist.add(taxi);
    }

    // remove
    public synchronized boolean remove(int idTaxi){
        for (Taxi taxi: taxislist) {
            if (taxi.getId() == idTaxi) {
                taxislist.remove(taxi);
                return true; // remove correctely
            }
        }
        return false; // NOT remove
    }

    public synchronized boolean checkTaxiIsAlreadyPresent(Taxi taxi) {
        // true - exist
        // false - NOT exist

        List<Taxi> taxisListCopy = getTaxisList();
        int id = taxi.getId();
        for (Taxi singleTaxi : taxisListCopy) {
            if (singleTaxi.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
