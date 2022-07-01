package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Taxi;
import Pollution.Measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaxiIstance {

    private List<Taxi> taxiList;
    private ArrayList<Measurement> averageListPollutionMeasurements;
    private static TaxiIstance instance;
    private int idCurrentTaxi;
    private int kmTravelled = 0;
    private int numberRides = 0;


    private boolean inElection = false;
    private boolean inRide = false;
    private boolean inExit = false;
    private Object rechargeLock = new Object(); // lock durante recharging
    private Object rideLock = new Object(); // lock durante ride
    private Object electionLock = new Object(); // lock durante election

    private int idRideOnRoad;
    private int idRideInElection;

    enum RechargeStatus {
        BATTERY_REQUESTED,
        BATTERY_IN_USED,
        BATTERY_NOT_IN_USED
    }

    private RechargeStatus inCharge = RechargeStatus.BATTERY_NOT_IN_USED;


    // Constructor
    private TaxiIstance() {
        taxiList = new ArrayList<Taxi>();
        averageListPollutionMeasurements = new ArrayList<Measurement>();
    }

    // Singleton
    public synchronized static TaxiIstance getInstance() {
        if (instance == null)
            instance = new TaxiIstance();
        return instance;
    }

    // Get taxis list
    public ArrayList<Taxi> getTaxiList() {
        synchronized (taxiList) {
            return new ArrayList<>(taxiList);
        }
    }

    // Add taxi to list
    public void addTaxi(Taxi t) {
        synchronized (taxiList) {
            taxiList.add(t);
            taxiList.notify();
        }
    }

    // remove taxi from list
    public void removeTaxi(Taxi taxi) {
        synchronized (taxiList) {
            taxiList.remove(taxi);
            taxiList.notify();
        }

        System.out.println(taxiList);
    }

    public void addAverageListPollutionMeasure(Measurement pm10) {
        synchronized (averageListPollutionMeasurements) {
            averageListPollutionMeasurements.add(pm10);
        }
    }

    public ArrayList<Measurement> getAverageListPollutionMeasurements() {
        return averageListPollutionMeasurements;
    }

    public int getKmTravelled() {
        return kmTravelled;
    }

    public int getIdCurrentTaxi() {
        return idCurrentTaxi;
    }

    public void setIdCurrentTaxi(int idCurrentTaxi) {
        this.idCurrentTaxi = idCurrentTaxi;
    }

    public Taxi getMyTaxi() {
        Taxi myTaxi = taxiList.stream().filter(t ->
                t.getId() == idCurrentTaxi).findFirst().orElse(null);
        return myTaxi;
    }

    public Position getPositionOfTaxi(int taxiId) {
        Taxi taxi = taxiList.stream().filter(t ->
                t.getId() == taxiId).findFirst().orElse(null);
        return taxi.getPosition();
    }

    public void updateSingleTaxi(int taxiId, Position position, int batteryLevel) {
        Taxi taxi = taxiList.stream().filter(t ->
                t.getId() == taxiId).findFirst().orElse(null);

        if (taxi != null) {
            taxi.setPosition(position);
            taxi.setBatteryLevel(batteryLevel);
        }

        System.out.println(position);
        System.out.println(position.getDistrictByPosition());
    }

    public void addKmTravelled(int kmTravelled) {
        this.kmTravelled = this.kmTravelled + kmTravelled;
    }

    public int kmTravelled() {
        return kmTravelled;
    }

    public void addNumberRides() {
        numberRides++;
    }

    public int getNumberRides() {
        return numberRides;
    }

    public boolean isInCharge() {
        if (inCharge == RechargeStatus.BATTERY_NOT_IN_USED) {
            return false;
        } else {
            return true;
        }
    }

    public void setInCharge(RechargeStatus inCharge) {
        this.inCharge = inCharge;
    }

    public boolean isInElection() {
        return inElection;
    }

    public void setInElection(boolean inElection) {
        this.inElection = inElection;
    }

    public boolean isInRide() {
        return inRide;
    }

    public void setInRide(boolean inRide) {
        this.inRide = inRide;
    }

    public boolean isInExit() {
        return inExit;
    }

    public void setInExit(boolean inExit) {
        this.inExit = inExit;
    }

    public RechargeStatus getInCharge() {
        return inCharge;
    }

    public Object getRechargeLock() {
        return rechargeLock;
    }

    public Object getRideLock() {
        return rideLock;
    }

    public Object getElectionLock() {
        return electionLock;
    }

    public int getIdRideOnRoad() {
        return idRideOnRoad;
    }

    public void setIdRideOnRoad(int idRideOnRoad) {
        this.idRideOnRoad = idRideOnRoad;
    }

    public int getIdRideInElection() {
        return idRideInElection;
    }

    public void setIdRideInElection(int idRideInElection) {
        this.idRideInElection = idRideInElection;
    }
}