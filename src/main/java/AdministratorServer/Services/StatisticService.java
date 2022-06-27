package AdministratorServer.Services;

import AdministratorServer.Model.Statistic;
import AdministratorServer.Model.Statistics;
import AdministratorServer.Model.Taxis;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("statistic")
public class StatisticService {

    // GET - http://localhost:1337/statistic
    @GET
    @Produces("application/json")
    public Response mainStatisticsService() {
        String string = "{\"server\": \"SETA\", \"environment\": \"production\", \"path\": \"statistic\"}";
        return Response.status(Response.Status.OK).entity(string).build();
    }

    // http://localhost:1337/statistic/add
    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addStatistic(Statistic statistic) {
        Statistics.getInstance().add(
                new Statistic(statistic.getNumberRides(),
                        statistic.getKmTravelled(),
                        statistic.getPollutionAverage(),
                        statistic.getBatteryLevel(),
                        statistic.getTimestamp(),
                        statistic.getIdTaxi())
        );

        String message = "{\"message\": \"statistic_add\", \"taxi\": " + statistic.getIdTaxi() + " }";
        return Response.status(Response.Status.CREATED).entity(message).build();
    }


    // ultime n statistiche di taxi con id
}
