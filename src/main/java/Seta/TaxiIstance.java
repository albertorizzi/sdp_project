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
        }
    }

    public void addAverageListPollutionMeasure(Measurement pm10) {
        synchronized (averageListPollutionMeasurements) {
            averageListPollutionMeasurements.add(pm10);
        }
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

        taxi.setPosition(position);
        taxi.setBatteryLevel(batteryLevel);
    }
}