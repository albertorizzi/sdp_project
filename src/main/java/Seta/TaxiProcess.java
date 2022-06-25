package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import Pollution.Measurement;
import Pollution.MeasuramentManager;
import Pollution.PM10Simulator;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class TaxiProcess {
    private static String BASE_URL = "http://localhost:1337/";
    private static Server grpc;

    public static void main(String[] args) {
        registrationMethod();
        //registrationMethod2();
        welcomeServer();
        // fromKeyboard();
    }

    private static void registrationMethod2() {
        System.out.println("ciao");

    }

    // REGISTRATION
    private static void registrationMethod() {
        String url = BASE_URL + "taxi/add";

        boolean success = false;

        // information of Taxi
        int randomId = 0;
        int min = 0, max = 1000;
        String addressServerAdministrator = "localhost";

        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response;
        JSONArray output = null;

        // https://support.microsoft.com/en-us/topic/how-to-configure-rpc-to-use-certain-ports-and-how-to-help-secure-those-ports-by-using-ipsec-2a94b798-063a-479a-8452-9cf07ac613d9
        int port = (int) Math.floor(Math.random() * (5000 - 1024 + 1) + 1024);

        /*while (success) {
            randomId = (int) Math.floor(Math.random() * (max - min + 1) + min);

            try {
                String bodyObject = "{\"id\":\"" + randomId + "\",\"addressServerAdministrator\":\"" + addressServerAdministrator + "\",\"portNumber\":\"" + port + "\"}";
                response = webResource.type("application/json").post(ClientResponse.class, bodyObject);
                output = response.getEntity(JSONArray.class);

                success = true;
            } catch (Exception e) {
                System.out.println("registrationMethod 1 - Error (IOException): " + e.getMessage());
            }
        }*/
        randomId = (int) Math.floor(Math.random() * (max - min + 1) + min);

        try {
            String bodyObject = "{\"id\":\"" + randomId + "\",\"addressServerAdministrator\":\"" + addressServerAdministrator + "\",\"portNumber\":\"" + port + "\"}";
            response = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, bodyObject);


            System.out.println(response.getStatus());
            output = response.getEntity(JSONArray.class);



            success = true;
        } catch (Exception e) {
            System.out.println("registrationMethod 1 - Error (IOException): " + e.getMessage());
        }

        Taxi taxi;

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

                if (taxiId == randomId) {
                    TaxiIstance.getInstance().setIdCurrentTaxi(taxiId);
                }
            } catch (JSONException e) {
                System.out.println("registrationMethod - JSONException: " + e.getMessage());
            }

        }

        System.out.println("🚖 taxiList: " + TaxiIstance.getInstance().getTaxiList());
        System.out.println("🚖 I'm Taxi: " + TaxiIstance.getInstance().getMyTaxi().getId());

        startPollutionSensors(); // startPollutionSensor

        // Taxi subscribe to the request of ride of own district
        TaxiPubSub taxiPubSub = new TaxiPubSub(TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition());
        taxiPubSub.start(); // thread start
    }


    private static void startPollutionSensors() {
        System.out.println("startPollutionSensors()");

        MeasuramentManager buffer = new MeasuramentManager();
        PM10Simulator pm10Simulator = new PM10Simulator(buffer);
        pm10Simulator.start();

        // consumer pollution
        ArrayList<Measurement> measurementList = new ArrayList<>(); // list of measurements
        ArrayList<Measurement> averageList = new ArrayList<>(); // list of average of measurement
        new Thread(() -> {
            while (true) {
                measurementList.addAll(buffer.readAllAndClean());
                double sum = 0;
                long timestamp = 0;
                int measurementId = 0;
                for (Measurement m : measurementList) {
                    sum += m.getValue();
                    timestamp = m.getTimestamp();
                }
                TaxiIstance.getInstance().addAverageListPollutionMeasure(
                        new Measurement("pm10-" + measurementId++, "PM10", sum / 8, timestamp)
                );
                measurementList.clear(); // clear list of measurement
            }
        }).start();
    }


    private static void welcomeServer() {
        System.out.println("welcomeServer()");

        try {
            grpc = ServerBuilder
                    .forPort(TaxiIstance.getInstance().getMyTaxi().getPortNumber())
                    .addService(new GrpcServiceImpl())
                    .build();
            grpc.start();
            System.out.println("👾 GRPC SERVER started!");

            if (!TaxiIstance.getInstance().getTaxiList().isEmpty()) {
                welcomeClient(); // create client only if taxiList is not empty
            }

            // grpc.awaitTermination();
        } catch (IOException e) {
            System.out.println("welcomeServer -IOException error");
            e.printStackTrace();
        }
    }

    private static void welcomeClient() {
        System.out.println("welcomeClient()");

        ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();

        for (Taxi taxi : taxiList) {
            if (taxi.getId() != TaxiIstance.getInstance().getMyTaxi().getId()) {
                System.out.println("🤝 Contacting Taxi " + taxi.getId() + "...");

                //opening a connection with the taxi's server
                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget(taxi.getAddressServerAdministrator() + ":" + taxi.getPortNumber())
                        .usePlaintext()
                        .build();

                GrpcServiceGrpc.GrpcServiceBlockingStub stub = GrpcServiceGrpc.newBlockingStub(channel);

                GrpcServiceOuterClass.Position position = GrpcServiceOuterClass.Position
                        .newBuilder()
                        .setX(TaxiIstance.getInstance().getMyTaxi().getPosition().getX())
                        .setY(TaxiIstance.getInstance().getMyTaxi().getPosition().getY())
                        .build();

                GrpcServiceOuterClass.HelloRequest request = GrpcServiceOuterClass.HelloRequest
                        .newBuilder()
                        .setId(TaxiIstance.getInstance().getMyTaxi().getId())
                        .setPort(TaxiIstance.getInstance().getMyTaxi().getPortNumber())
                        .setIp(TaxiIstance.getInstance().getMyTaxi().getAddressServerAdministrator())
                        .setPosition(position)
                        .setBatteryLevel(TaxiIstance.getInstance().getMyTaxi().getBatteryLevel())
                        .build();

                GrpcServiceOuterClass.HelloResponse response;
                try {
                    response = stub.greeting(request);
                    System.out.println(response);
                } catch (Exception e) {
                    //System.out.println("ERRORE: " + e.getMessage());
                    System.out.println("🔴 welcomeClient - Non riesco a contattare il drone " + taxi.getId());

                    TaxiIstance.getInstance().removeTaxi(taxi);
                }
                channel.shutdownNow();
            }
        }
    }





    private static void sendStatsToServer() {
        // TODO: inviare statistica al server
        System.out.println("inviare statistiche al server, da fare");
    }
}
