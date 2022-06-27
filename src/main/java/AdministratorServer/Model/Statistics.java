package AdministratorServer.Model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistics {

    @XmlElement(name = "statistics")
    private final List<Statistic> statisticListist;

    private static Statistics instance;

    // constructor
    private Statistics() {
        statisticListist = new ArrayList<Statistic>();
    }

    //singleton
    public synchronized static Statistics getInstance() {
        if (instance == null)
            instance = new Statistics();
        return instance;
    }

    public synchronized void add(Statistic statistic) {
        statisticListist.add(statistic);
    }


}
