package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Taxi;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import Pollution.Measurement;
import Pollution.MeasuramentManager;
import Pollution.PM10Simulator;

import java.util.ArrayList;

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
            String bodyObject = "{\"id\":\"" + randomId + "\",\"addressServerAdministrator\":\"" + addressServerAdministrator + "\",\"portNumber\":\"" + port + "\"}";
            response = webResource.type("application/json").post(ClientResponse.class, bodyObject);
            System.out.println("response: " + response);


            output = response.getEntity(JSONArray.class);
            System.out.println("output: " + output);

        } catch (Exception e) {
            System.out.println("registrationMethod 1 - Error (IOException): " + e.getMessage());
        }

        Taxi taxi;
        Position position;

        for (int i = 0; i < output.length(); i++) {
            try {

                JSONObject jsonObject = (JSONObject) output.get(i);
                int taxiId;
                taxiId = (int) jsonObject.get("id");
                String taxiAddressServerAdministrator = (String) jsonObject.get("addressServerAdministrator");
                int taxiPortNumber = (int) jsonObject.get("portNumber");
                int x = Integer.parseInt(jsonObject.get("position").toString().substring(5, 6));
                int y = Integer.parseInt(jsonObject.get("position").toString().substring(11, 12));
                Position taxiPosition = new Position(x, y);
                int taxiBattery = (int) jsonObject.get("batteryLevel");

                taxi = new Taxi(taxiId, taxiPortNumber, taxiAddressServerAdministrator, taxiBattery, taxiPosition);


                TaxiIstance.getInstance().addTaxi(taxi);


            } catch (JSONException e) {
                System.out.println("registrationMethod 2 - JSONException: " + e.getMessage());
            }

        } // for
        System.out.println("🚖 taxiList: " + TaxiIstance.getInstance().getTaxiList());

        // TODO: cambiare commenti. Quando l‘inserimento del drone nella smart-city va a buon fine,
        // esso dovrà avviare il proprio sensore per il rilevamento dell’inquinamento dell’aria.
        startPollutionSensors(); // 3.1 AVVIO SENSORE INQUINAMENTO
    }


    private static void startPollutionSensors() {
        System.out.println("startPollutionSensors()");

        MeasuramentManager buffer = new MeasuramentManager();
        PM10Simulator pm10Simulator = new PM10Simulator(buffer);
        pm10Simulator.start();

        // consumatore


        ArrayList<Measurement> measurementList = new ArrayList<>(); // lista delle misurazioni
        //ArrayList<Measurement> averageList = new ArrayList<>(); // lista delle medie delle misurazioni
        new Thread(() -> { // lamba expression
            while (true) {

                measurementList.addAll(buffer.readAllAndClean());
                double sum = 0;
                long timestamp = 0;
                int measurementId = 0;
                for (Measurement m : measurementList) {
                    //System.out.println("m: " + m.getValue()); // debug
                    sum += m.getValue();
                    timestamp = m.getTimestamp();
                }
                TaxiIstance.getInstance().addAverageList(
                        new Measurement("pm10-" + measurementId++, "PM10", sum / 8, timestamp)
                );
                measurementList.clear(); //sum = 0;
                //System.out.println("averageList: " + averageList); // debug

            }
        }).start();


    } // startSimulator


}
