package Seta;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class TaxiProcess {
    private static String BASE_URL = "http://localhost:1337/";

    public static void main(String[] args) {
        registrationMethod();
    }


    // REGISTRATION
    private static void registrationMethod() {
        String url = BASE_URL + "taxi/add";

        boolean success = false;

        // information of Taxi
        int randomId;
        int min = 0, max = 1000;
        String addressServerAdministrator = "http://localhost";

        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response;
        JSONArray output = null;

        int port = (int) Math.floor(Math.random() * (65535 - 49152 + 1) + 49152);
        randomId = (int) Math.floor(Math.random() * (max - min + 1) + min);


        try {
            String bodyObject =  "{\"id\":\"" + randomId + "\",\"addressServerAdministrator\":\"" + addressServerAdministrator + "\",\"portNumber\":\"" + port + "\"}";


            response = webResource.type("application/json").post(ClientResponse.class, bodyObject);
            System.out.println("response: " + response); // debug



            output = response.getEntity(JSONArray.class);
            System.out.println("output: " + output); // debug



        } catch (Exception e) {
            System.out.println("registrationMethod 1 - Error (IOException): " + e.getMessage());
        }


        // aggiungo i droni dalla lista
    }
}
