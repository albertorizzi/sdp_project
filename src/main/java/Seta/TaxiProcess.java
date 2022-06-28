package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Statistic;
import AdministratorServer.Model.Taxi;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import com.google.gson.Gson;
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
    private static TaxiPubSub taxiPubSub;

    public static void main(String[] args) {
        registrationMethod();
        welcomeServer();
        manageInputFromKeyboard();
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

        while (!success) {
            randomId = (int) Math.floor(Math.random() * (max - min + 1) + min);

            try {
                String bodyObject = "{\"id\":\"" + randomId + "\",\"addressServerAdministrator\":\"" + addressServerAdministrator + "\",\"portNumber\":\"" + port + "\"}";
                response = webResource.type("application/json").post(ClientResponse.class, bodyObject);

                if (response.getStatus() == 201) {
                    output = response.getEntity(JSONArray.class);
                } else {
                    System.out.println("Server error in POST taxi/add");
                }

                success = true;
            } catch (Exception e) {
                System.out.println("registrationMethod 1 - Error (IOException): " + e.getMessage());
            }
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
        startStatisticsToServer();

        // Taxi subscribe to the request of ride of own district
        taxiPubSub = new TaxiPubSub(TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition());
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

    private static void manageInputFromKeyboard() {

        new Thread(() -> { // lambda expression
            while (true) {
                Scanner scanner = new Scanner(System.in);
                //System.out.print("⏹ Send quit to stop the drone process...\n");
                String input = scanner.next();

                switch (input) {
                    case "quit":
                        try {
                            explicitClosure();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        break;
                    case "re":
                        System.out.print("⚡️ recharge request\n");
                        if (TaxiIstance.getInstance().isInExit())
                            System.out.println("⚡️ I can't recharge, I'm quitting");
                        else {
                            try {
                                rechargeTaxiBattery();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    default:
                        System.out.println("⚠️ Input NOT VALID");
                }
            }
        }).start();

    }

    private static void rechargeTaxiBattery() throws InterruptedException {
        System.out.println("\n♻️🔋 RECHARGE Taxi requested...");

        // if a Taxi is in an election process, can't quit
        if (TaxiIstance.getInstance().isInElection()) {
            synchronized (TaxiIstance.getInstance().getElectionLock()) {
                while (TaxiIstance.getInstance().isInElection()) {
                    try {
                        System.out.println("⏹🗳 I can't recharge, I'm inside an ELECTION...WAIT");
                        TaxiIstance.getInstance().getElectionLock().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("⏹🗳 I'm not inside an election anymore.");
            }
        }

        // if a Taxi is in a Ride process, can't quit
        if (TaxiIstance.getInstance().isInRide()) {
            synchronized (TaxiIstance.getInstance().getRideLock()) {
                while (TaxiIstance.getInstance().isInRide()) {
                    try {
                        System.out.println("⏹🚕 I can't recharge, I'm in a RIDE...WAIT");
                        TaxiIstance.getInstance().getRideLock().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("⏹🚕 I'm not riding anymore.");
            }
        }

        TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_REQUESTED);

        ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();
        int countElection = 0;

        Date date = new Date();
        long timestamp = date.getTime(); // timestamp in ms

        Position newTaxiPosition = TaxiIstance.getInstance().getMyTaxi().getPosition();

        int updateBatteryLevel = 100;

        if (taxiList.size() == 1) { // I'm only the one Taxi in SETA
            System.out.println("♻️🪫️ RECHARGE station in " + TaxiIstance.getInstance().getMyTaxi().getPosition().getDistrictByPosition() + " district WON by Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());

            // recharge
            TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_IN_USED);

            System.out.println("⚡️ Charging...");
            Thread.sleep(10000); // 10 seconds
            System.out.println("⚡️ Battery completed...");

            synchronized (TaxiIstance.getInstance().getRechargeLock()) {
                TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_NOT_IN_USED);
                TaxiIstance.getInstance().getRechargeLock().notify();
            }
        } else {
            for (Taxi taxi : taxiList) {
                if (taxi.getId() != TaxiIstance.getInstance().getMyTaxi().getId()) {
                    //opening a connection with the taxi's server
                    final ManagedChannel channel = ManagedChannelBuilder
                            .forTarget(taxi.getAddressServerAdministrator() + ":" + taxi.getPortNumber())
                            .usePlaintext()
                            .build();

                    GrpcServiceGrpc.GrpcServiceBlockingStub stub = GrpcServiceGrpc.newBlockingStub(channel);

                    GrpcServiceOuterClass.Position position = GrpcServiceOuterClass.Position
                            .newBuilder()
                            .setX(newTaxiPosition.getX())
                            .setY(newTaxiPosition.getY())
                            .build();

                    GrpcServiceOuterClass.SendRechargeTaxiRequest request = GrpcServiceOuterClass.SendRechargeTaxiRequest
                            .newBuilder()
                            .setIdTaxi(taxi.getId())
                            .setRechargeStation(position)
                            .setTimestamp(timestamp)
                            .build();

                    GrpcServiceOuterClass.ReplyRechargeTaxiResponse response;

                    try {
                        response = stub.recharge(request);

                        if (response.getMessageResponse().equals("OK")) {
                            countElection++;
                        } else if (response.getMessageResponse().equals("NO")) {
                            break;
                        }

                        if (countElection == taxiList.size() - 1) {
                            System.out.println("♻️🪫️ RECHARGE station in " + newTaxiPosition.getDistrictByPosition() + " districs WON by Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());

                            // recharge
                            TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_IN_USED);

                            System.out.println("⚡️ Charging...");
                            Thread.sleep(10000); // 10 seconds
                            System.out.println("⚡️ Battery completed...");

                            ArrayList<Integer> arr = newTaxiPosition.getPositionOfRechargeStationByDistrict();
                            newTaxiPosition = new Position(arr.get(0), arr.get(1)); // management of position because Jersey didn't work using new Position

                            synchronized (TaxiIstance.getInstance().getRechargeLock()) {
                                TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_NOT_IN_USED);
                                TaxiIstance.getInstance().getRechargeLock().notify();
                            }
                        } else {
                            System.out.println("❌ 🪫 RECHARGE station NOT WON by Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ TaxiProcess.rechargeTaxiBattery - I can't contact taxi with ID: " + taxi.getId());
                        TaxiIstance.getInstance().removeTaxi(taxi);
                    }
                    channel.shutdownNow();
                }
            }
        }

        // set new batteryLevel, position
        TaxiIstance.getInstance().getMyTaxi().setBatteryLevel(updateBatteryLevel);
        TaxiIstance.getInstance().getMyTaxi().setPosition(newTaxiPosition);

        // update other taxi with my new data
        if (taxiList.size() == 1) {
            System.out.println("🚖 I'm the only Taxi, I don't update anyone!");
        } else {
            System.out.println("🚖 I contact other Taxis to update my info");

            for (Taxi taxi : taxiList) {
                if (taxi.getId() != TaxiIstance.getInstance().getMyTaxi().getId()) { // check if taxi is unequal of iteration
                    //opening a connection with the taxi's server
                    final ManagedChannel channel = ManagedChannelBuilder
                            .forTarget(taxi.getAddressServerAdministrator() + ":" + taxi.getPortNumber())
                            .usePlaintext()
                            .build();

                    GrpcServiceGrpc.GrpcServiceBlockingStub stub = GrpcServiceGrpc.newBlockingStub(channel);

                    GrpcServiceOuterClass.Position position = GrpcServiceOuterClass.Position
                            .newBuilder()
                            .setX(newTaxiPosition.getX())
                            .setY(newTaxiPosition.getY())
                            .build();

                    GrpcServiceOuterClass.TaxiInfoAfterRideRequest request = GrpcServiceOuterClass.TaxiInfoAfterRideRequest
                            .newBuilder()
                            .setIdTaxi(TaxiIstance.getInstance().getMyTaxi().getId())
                            .setBatteryLevel(updateBatteryLevel)
                            .setFinalPosition(position)
                            .build();

                    GrpcServiceOuterClass.TaxiInfoAfterRideResponse response;
                    try {
                        response = stub.notifyTaxisAfterRide(request);
                        //System.out.println(response);
                    } catch (Exception e) {
                        System.out.println("⚠️ TaxiProcess.rechargeTaxiBattery - I can't contact taxi with ID: " + taxi.getId());
                        TaxiIstance.getInstance().removeTaxi(taxi);
                    }
                    channel.shutdownNow();
                }

            }
        }
    }

    public static void explicitClosure() throws IOException, InterruptedException {
        /*
            1. complete the possible ride it is involved in, sending to the Administra-
                tor Server the information described in Section 5.2.2
            2. complete any battery recharge
            3. notify the other taxis of the smart city
            4. request the Administrator Server to leave the smart city
         */


        if (TaxiIstance.getInstance().isInExit()) {
            return;
        }
        TaxiIstance.getInstance().setInExit(true);

        System.out.println("\n⏹ STOPPING Taxi...");

        // if a Taxi is in an election process, can't quit
        synchronized (TaxiIstance.getInstance().getElectionLock()) {
            while (TaxiIstance.getInstance().isInElection()) {
                try {
                    System.out.println("⏹🗳 I can't quit, I'm inside an ELECTION... WAIT");
                    TaxiIstance.getInstance().getElectionLock().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("⏹🗳 I'm not inside an election anymore");
        }

        // if a Taxi is in a ride, can't quit
        synchronized (TaxiIstance.getInstance().getRideLock()) {
            while (TaxiIstance.getInstance().isInRide()) {
                try {
                    System.out.println("⏹🚕 I can't quit, I'm in a RIDE... WAIT");
                    TaxiIstance.getInstance().getRideLock().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("⏹🚕 I'm not RIDING anymore.");
        }

        // complete recharge
        rechargeTaxiBattery();


        // disconnection with BROKER
        taxiPubSub.disconnectClient();

        // Send stats ti REST server
        System.out.println("⏹ Sending last stats to the REST Server StatisticService...");
        sendStatsToServer();

        // close communication (chennal grpc) with other Taxi
        System.out.println("⏹ Shutting down the Grpc Server...");
        try {
            grpc.shutdownNow();
        } catch (Exception e) {
            System.out.println("Error in closing server: " + e);
        }

        // communication with Server REST
        System.out.println("⏹ Contacting the REST Server...");
        int id = TaxiIstance.getInstance().getMyTaxi().getId();
        try {
            URL url = new URL("http://localhost:1337/taxi/remove/" + id);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

            bufferedReader.close();
            urlConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 5. SUPER USCITA
        System.exit(0);
    }

    private static void startStatisticsToServer() {
        System.out.println("📊 Starting sending statistics to the SERVER...");

        new Thread(() -> {
            while (true) {
                sendStatsToServer();
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void sendStatsToServer() {
        System.out.println("inviare statistiche al server, da fare");

        String url = BASE_URL + "statistic/add";

        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response;
        JSONArray output = null;

        Date date = new Date();
        long actualTime = date.getTime(); // timestamp in ms

        ArrayList<Double> lis = new ArrayList<Double>(
        );

        for (Measurement measurement : TaxiIstance.getInstance().getAverageListPollutionMeasurements()
        ) {
            lis.add(measurement.getValue());


        }


        try {


            Statistic s = new Statistic(
                    TaxiIstance.getInstance().getNumberRides(),
                    TaxiIstance.getInstance().getKmTravelled(),
                    lis,
                    TaxiIstance.getInstance().getMyTaxi().getBatteryLevel(),
                    actualTime,
                    TaxiIstance.getInstance().getMyTaxi().getId()
            );

            String input = new Gson().toJson(s);

            System.out.println(input);


            response = webResource.type("application/json").post(ClientResponse.class, input);

            if (response.getStatus() == 201) {
                output = response.getEntity(JSONArray.class);

                System.out.println(output);


            } else {
                System.out.println("Server error in POST statistic/add");
            }


        } catch (Exception e) {
            System.out.println("registrationMethod 1 - Error (IOException): " + e.getMessage());
        }


    }
}
