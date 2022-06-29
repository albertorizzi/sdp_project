package AdministratorServer.Services;

import AdministratorServer.Model.Statistic;
import AdministratorServer.Model.Statistics;
import AdministratorServer.Model.Taxis;
import Seta.TaxiIstance;
import org.codehaus.jettison.json.JSONException;

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
    @Consumes({"application/json"})
    @Produces({"application/json", "application/xml"})
    public Response addStatistic(Statistic statistic) {

        System.out.println(statistic.toString());

        Statistics.getInstance().add(statistic);

        String message = "{\"message\": \"statistic_add\", \"taxi\": " + statistic.getIdTaxi() + " }";
        return Response.status(Response.Status.CREATED).entity(message).build();
    }


    // last n statistics of taxi wit determinate id
    // http://localhost:1337/statistic/4/taxi/465
    @Path("{nLastStat}/taxi/{idTaxi}")
    @GET
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response getLastNStatisticsOfDeterminatedIdTaxi(@PathParam("nLastStat") int lastNStat, @PathParam("idTaxi") int idTaxi) throws JSONException {

        if (Taxis.getInstance().checkTaxiIsAlreadyPresentById(idTaxi)) {
            // check size lenght of Statistic
            if (Statistics.getInstance().getStatisticList().size() == 0) {
                return Response.status(Response.Status.NO_CONTENT).entity("{\"message\": \"NO_STATISTICS.\"}").build();
            } else if (Statistics.getInstance().getStatisticList().size() < lastNStat) {
                return Response.status(Response.Status.NO_CONTENT).entity("{\"message\": \"SIZE_STATISTICS_LOWER_THAN_LAST_N_STAT.\"}").build();
            } else {
                return Response.ok(Statistics.getInstance().getNStatistics(lastNStat, idTaxi)).build();
            }
        } else {
            String message = "{\"message\": \"taxi_not_present\", \"taxi\": " + idTaxi + " }";
            return Response.status(Response.Status.NOT_FOUND).entity(message).build();
        }
    }

    // The average of the previously introduced statistics provided by all the taxis and occurred from timestamps t1 and t2
    // http://localhost:1337/statistics/get/deliveries
    @Path("/timestamp/{timestamp}") //timestamp: 0000000-0000000
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getStatisticsBetweenTimestamp(@PathParam("timestamp") String timestamp) throws JSONException {

        if(Statistics.getInstance().getStatisticList().size() == 0){
            return Response.status(Response.Status.NO_CONTENT).entity("{\"message\": \"NO_STATSTICS.\"}").build();
        } else {
            //System.out.println("StatisticsServices " + t); // debug
            long t1 = Long.parseLong(timestamp.split("-")[0]);
            long t2 = Long.parseLong(timestamp.split("-")[1]);
            System.out.println("StatisticsServices " + t1); // debug
            System.out.println("StatisticsServices " + t2); // debug
            return Response.ok(Statistics.getInstance().getStatisticsBetweenTimestamp(t1, t2)).build();
        }
    }
}
