package AdministratorServer.Model;

import java.util.ArrayList;
import java.util.List;

public class Rides {
    private final List<Ride> ridesQueue;

    private static Rides instance;

    // constructor
    private Rides() {
        ridesQueue = new ArrayList<Ride>();
    }

    //singleton
    public synchronized static Rides getInstance() {
        if (instance == null)
            instance = new Rides();
        return instance;
    }

    public synchronized List<Ride> getRidesQueue() {
        return new ArrayList<Ride>(ridesQueue);
    }

    public synchronized void add(Ride ride) {
        ridesQueue.add(ride);
    }

    public synchronized boolean remove(int idRide) {
        // true - correct remove
        // false - NOT exist, nothing to remove
        for (Ride ride : ridesQueue) {
            if (ride.getIDRide() == idRide) {
                ridesQueue.remove(ride);
                return true;
            }
        }
        return false;
    }
}
