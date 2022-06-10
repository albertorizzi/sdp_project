package AdministratorServer.Services;


import AdministratorServer.Model.Taxi;
import AdministratorServer.Model.Taxis;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;


@Path("taxi")
public class AdministratorServer {
    @GET
    @Produces("application/json")
    public Response mainTaxiService() {
        String string = "{\"server\": \"SETA\", \"environment\": \"production\"}";
        return Response.status(Response.Status.OK).entity(string).build();
    }

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json"})
    public Response addTaxi(Taxi taxi) {
        /*
        When a taxi requests to join the network, the Administrator Server:
            • Tries to add the taxi to the smart-city
            • If a taxi with the same identifier already exists, an error message is returned
            • Otherwise, the taxi is added to the list of taxis and the Administrator Server returns to that taxi:
                • The position of the recharge station of a randomly chosen district
                • The list of taxis already registered in the smart-city
         */

        boolean taxiInList = Taxis.getInstance().checkTaxiIsAlreadyPresent(taxi);

        System.out.println(taxiInList);

        if (taxiInList) {
            String message = "{\"message\": \"TAXI with ID=" + taxi.getId() + " already exist.\"}";
            return Response.status(Response.Status.CONFLICT).entity(message).build();
        } else {
            Taxis.getInstance().add(taxi);
            return Response.status(Response.Status.OK).entity(Taxis.getInstance().getTaxisList()).build();
        }


    }


}
