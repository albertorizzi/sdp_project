package AdministratorServer.Model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistic {

    private int numberRides;
    private int kmTravelled;
    private float pollutionAverage;
    private int batteryLevel;
    private long timestamp;
    private int idTaxi;

    // empty constructor
    public Statistic(){};

    public Statistic(int numberRides, int kmTravelled, float pollutionAverage, int batteryLevel, long timestamp, int idTaxi) {
        this.numberRides = numberRides;
        this.kmTravelled = kmTravelled;
        this.pollutionAverage = pollutionAverage;
        this.batteryLevel = batteryLevel;
        this.timestamp = timestamp;
        this.idTaxi = idTaxi;
    }

    public int getNumberRides() {
        return numberRides;
    }

    public void setNumberRides(int numberRides) {
        this.numberRides = numberRides;
    }

    public int getKmTravelled() {
        return kmTravelled;
    }

    public void setKmTravelled(int kmTravelled) {
        this.kmTravelled = kmTravelled;
    }

    public float getPollutionAverage() {
        return pollutionAverage;
    }

    public void setPollutionAverage(float pollutionAverage) {
        this.pollutionAverage = pollutionAverage;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getIdTaxi() {
        return idTaxi;
    }

    public void setIdTaxi(int idTaxi) {
        this.idTaxi = idTaxi;
    }
}
