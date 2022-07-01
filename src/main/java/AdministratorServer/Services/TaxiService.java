package AdministratorServer.Services;

import AdministratorServer.Model.Taxi;
import AdministratorServer.Model.Taxis;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("taxi")
public class TaxiService {

    // GET - http://localhost:1337/taxi
    @GET
    @Produces("application/json")
    public Response mainTaxiService() {
        String string = "{\"server\": \"SETA\", \"environment\": \"production\", \"path\": \"taxi\"}";
        return Response.status(Response.Status.OK).entity(string).build();
    }

    // GET - http://localhost:1337/taxi/all
    @Path("all")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTaxisList() {
        Gson gson = new Gson();
        Taxis taxis = Taxis.getInstance();
        System.out.println(taxis);
        return Response.status(Response.Status.OK).entity(gson.toJson(taxis)).build();
    }

    // POST - http://localhost:1337/taxi/add
    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces("application/json")
    public Response addTaxi(Taxi taxi) {
        /*
        When a taxi requests to join the network, the Administrator Server:
            • Tries to add the taxi to the smart-city
            • If a taxi with the same identifier already exists, an error message is returned
            • Otherwise, the taxi is added to the list of taxis and the Administrator Server returns to that taxi:
                • The position of the recharge station of a randomly chosen district
                • The list of taxis already registered in the smart-city
         */
        System.out.println(taxi);
        boolean taxiInList = Taxis.getInstance().checkTaxiIsAlreadyPresent(taxi);

        if (taxiInList) {
            String message = "{\"message\": \"TAXI with ID=" + taxi.getId() + " already exist.\"}";
            return Response.status(Response.Status.CONFLICT).entity(message).build();
        } else {
            Taxis.getInstance().add(taxi);
            return Response.status(Response.Status.CREATED).entity(Taxis.getInstance().getTaxisList()).build();
            //   return Response.ok(Taxis.getInstance().getTaxisList()).build();

        }
    }


    // http://localhost:1337/taxi/remove/1
    @Path("remove/{id}")
    @DELETE
    @Produces("text/plain")
    public Response removeTaxi(@PathParam("id") int idTaxi){
        if (Taxis.getInstance().remove(idTaxi)) {
            return Response.ok("✅ Taxi removed successfully!").build();
        } else  {
            return Response.status(Response.Status.NOT_FOUND).entity("Taxi" + idTaxi + " doesn't exist.").build();
        }
    }
}
