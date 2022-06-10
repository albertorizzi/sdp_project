package Seta;

import AdministratorServer.Model.Taxi;
import Pollution.Measurement;

import java.util.ArrayList;
import java.util.List;

public class TaxiIstance {

    private List<Taxi> taxiList;
    private ArrayList<Measurement> averageListPollutionMeasurements;
    private static TaxiIstance instance;

    // Constructor
    private TaxiIstance() {
        taxiList = new ArrayList<Taxi>();
        averageListPollutionMeasurements = new ArrayList<Measurement>();
    }

    // Singleton
    public synchronized static TaxiIstance getInstance(){
        if(instance==null)
            instance = new TaxiIstance();
        return instance;
    }

    // Get taxis list
    public ArrayList<Taxi> getTaxiList(){
        synchronized(taxiList) {
            return new ArrayList<>(taxiList);
        }
    }

    // Add taxi to list
    public void addTaxi(Taxi t){
        synchronized (taxiList) {
            taxiList.add(t);
        }
    }

    public void addAverageListPollutionMeasure(Measurement pm10) {
        synchronized (averageListPollutionMeasurements){
            averageListPollutionMeasurements.add(pm10);
        }
    }
}