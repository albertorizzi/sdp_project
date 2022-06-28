package AdministratorServer.Model;

import Pollution.Measurement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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

    public synchronized ArrayList<Float> getNStatistics(int numberStatistics, int idTaxi) {
        int fromIndex = statisticList.size() - numberStatistics;
        int toIndex = statisticList.size();


        List<Statistic> statisticListFilteredByIdTaxi = statisticList.stream().filter(stat ->
                        stat.getIdTaxi() == idTaxi)
                .collect(Collectors.toList());

        statisticListFilteredByIdTaxi.subList(fromIndex, toIndex);

        float kmTravelled;
        float batteryLevel;
        float numberRides;
        float pollutionLevel;

        float sumKmTravelled = 0;
        float sumBatteryLevel = 0;
        float sumNumberRides = 0;
        List<Measurement> concatMeasurament = new ArrayList<>();


        for (Statistic statistic : statisticListFilteredByIdTaxi) {
            sumKmTravelled += statistic.getKmTravelled();
            sumBatteryLevel += statistic.getBatteryLevel();
            sumNumberRides += statistic.getNumberRides();

            concatMeasurament.addAll(statistic.getPollutionAverage());
        }

        kmTravelled = sumKmTravelled / statisticList.size();
        batteryLevel = sumBatteryLevel / statisticList.size();
        numberRides = sumNumberRides / statisticList.size();

        float sumPollution = 0;

        for (Measurement measurement : concatMeasurament) {
            sumPollution += measurement.getValue();
        }

        pollutionLevel = sumPollution / concatMeasurament.size();


        ArrayList<Float> resultAverageNStat = new ArrayList<Float>();
        resultAverageNStat.add(kmTravelled);
        resultAverageNStat.add(batteryLevel);
        resultAverageNStat.add(numberRides);
        resultAverageNStat.add(pollutionLevel);

        return resultAverageNStat;

    }


}
