package AdministratorServer.Model;

import Pollution.Measurement;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistics {

    @XmlElement(name = "statistics")
    private final List<Statistic> statisticList;

    private static Statistics instance;

    // constructor
    private Statistics() {
        statisticList = new ArrayList<Statistic>();
    }

    //singleton
    public synchronized static Statistics getInstance() {
        if (instance == null)
            instance = new Statistics();
        return instance;
    }

    public synchronized void add(Statistic statistic) {
        statisticList.add(statistic);
    }

    public List<Statistic> getStatisticList() {
        return statisticList;
    }

    public synchronized String getNStatistics(int numberStatistics, int idTaxi) throws JSONException {
        List<Statistic> statisticListFilteredByIdTaxi = statisticList.stream().filter(stat ->
                        stat.getIdTaxi() == idTaxi)
                .collect(Collectors.toList());

        System.out.println(statisticListFilteredByIdTaxi.size());

        int fromIndex = statisticListFilteredByIdTaxi.size() - numberStatistics;
        int toIndex = statisticListFilteredByIdTaxi.size();

        statisticListFilteredByIdTaxi.subList(fromIndex, toIndex);

        float kmTravelled;
        float batteryLevel;
        float numberRides;
        float pollutionLevel;

        float sumKmTravelled = 0;
        float sumBatteryLevel = 0;

        List<Measurement> concatMeasurament = new ArrayList<>();


        for (Statistic statistic : statisticListFilteredByIdTaxi) {
            sumKmTravelled += statistic.getKmTravelled();
            sumBatteryLevel += statistic.getBatteryLevel();

            concatMeasurament.addAll(statistic.getPollutionAverage());
        }

        kmTravelled = sumKmTravelled / statisticListFilteredByIdTaxi.size();
        batteryLevel = sumBatteryLevel / statisticListFilteredByIdTaxi.size();
        numberRides = statisticListFilteredByIdTaxi.get(statisticListFilteredByIdTaxi.size() - 1).getNumberRides();

        float sumPollution = 0;

        for (Measurement measurement : concatMeasurament) {
            sumPollution += measurement.getValue();
        }

        if (concatMeasurament.size() == 0) { // to manage if size is 0
            pollutionLevel = 0;
        } else {
            pollutionLevel = sumPollution / concatMeasurament.size();
        }

        JSONObject response = new JSONObject();

        DecimalFormat df = new DecimalFormat("###.####");

        response.put("kmTravelled", df.format(kmTravelled));
        response.put("batteryLevel", df.format(batteryLevel));
        response.put("numberRides", df.format(numberRides));
        response.put("pollutionLevel", df.format(pollutionLevel));

        return response.toString();
    }

    public synchronized String getStatisticsBetweenTimestamp(long timestampStart, long timestampEnd) throws JSONException {
        List<Statistic> statisticListFilteredByTimestamp = statisticList.stream().filter(
                        stat -> stat.getTimestamp() > timestampStart && stat.getTimestamp() < timestampEnd)
                .collect(Collectors.toList());

        float kmTravelled;
        float batteryLevel;
        float numberRides;
        float pollutionLevel;

        float sumKmTravelled = 0;
        float sumBatteryLevel = 0;

        List<Measurement> concatMeasurament = new ArrayList<>();

        for (Statistic statistic : statisticListFilteredByTimestamp) {
            sumKmTravelled += statistic.getKmTravelled();
            sumBatteryLevel += statistic.getBatteryLevel();

            concatMeasurament.addAll(statistic.getPollutionAverage());
        }

        kmTravelled = sumKmTravelled / statisticListFilteredByTimestamp.size();
        batteryLevel = sumBatteryLevel / statisticListFilteredByTimestamp.size();
        numberRides = statisticListFilteredByTimestamp.get(statisticListFilteredByTimestamp.size() - 1).getNumberRides();

        float sumPollution = 0;

        for (Measurement measurement : concatMeasurament) {
            sumPollution += measurement.getValue();
        }

        if (concatMeasurament.size() == 0) { // to manage if size is 0
            pollutionLevel = 0;
        } else {
            pollutionLevel = sumPollution / concatMeasurament.size();
        }

        JSONObject response = new JSONObject();

        DecimalFormat df = new DecimalFormat("###.####");

        response.put("kmTravelled", df.format(kmTravelled));
        response.put("batteryLevel", df.format(batteryLevel));
        response.put("numberRides", df.format(numberRides));
        response.put("pollutionLevel", df.format(pollutionLevel));

        return response.toString();
    }

    public synchronized String getTimestamp() throws JSONException {
        JSONObject response = new JSONObject();

        response.put("timestampStart", statisticList.get(0).getTimestamp());
        response.put("timestampEnd", statisticList.get(statisticList.size()-1).getTimestamp());

        return response.toString();
    }
}
