package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import Utils.Utils;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import com.sun.javafx.scene.traversal.SubSceneTraversalEngine;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jdk.nashorn.internal.runtime.ECMAException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.Date;

public class TaxiPubSub extends Thread {
    private static MqttClient client;
    private int districtNumber;
    private String BASE_TOPIC = "seta/smartcity/rides/district";
    private String topic;
    private String broker = "tcp://localhost:1883";


    public TaxiPubSub(int districtNumber) {
        this.districtNumber = districtNumber;
        this.topic = BASE_TOPIC + districtNumber;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getDistrictNumber() {
        return districtNumber;
    }

    public void setDistrictNumber(int districtNumber) {
        this.districtNumber = districtNumber;
    }

    public void run() {
        System.out.println("🚚 TaxiPubSub - Starting Broker MQTT...");

        String clientId = MqttClient.generateClientId();
        int qos = 2;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setMaxInflight(500);
            connOpts.setConnectionTimeout(0);

            client.connect(connOpts); // synchronous

            client.setCallback(new MqttCallback() {
                // messageArrived: reception of message of specific topic on this there's a subscription
                public void messageArrived(String topic, MqttMessage message) throws JSONException, MqttException, InterruptedException {
                    // Called when a message arrives from the server that matches any subscription made by the client
                    String receivedMessage = new String(message.getPayload()); // from binary to string

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject = new JSONObject(receivedMessage);
                    } catch (JSONException err) {
                        err.printStackTrace();
                    }

                    // obtain a ride from JSONobj
                    JSONObject rideObject = (JSONObject) jsonObject.get("ride");
                    int idRide = Integer.parseInt((String) rideObject.get("id"));

                    Position startPosition = new Position(
                            Integer.parseInt((String) rideObject.getJSONObject("startPosition").get("x")),
                            Integer.parseInt((String) rideObject.getJSONObject("startPosition").get("y"))
                    );

                    Position destinationPosition = new Position(
                            Integer.parseInt((String) rideObject.getJSONObject("destinationPosition").get("x")),
                            Integer.parseInt((String) rideObject.getJSONObject("destinationPosition").get("y"))
                    );

                    Ride ride = new Ride(idRide, startPosition, destinationPosition);

                    if (ride.getStartPosition().getDistrictByPosition() == districtNumber) {
                        System.out.println("\n" + "📍 New Ride -> id: " + ride.getIDRide() + " district: " + ride.getStartPosition().getDistrictByPosition());

                        // check if I'm riding or I'm busy in a ride or is in an election
                        if (TaxiIstance.getInstance().isInCharge() || TaxiIstance.getInstance().isInRide() || TaxiIstance.getInstance().isInElection()) {
                            System.out.println("🚕 NOT manage ride " + ride);
                            System.out.println("isInCharge(): " + TaxiIstance.getInstance().isInCharge());
                            System.out.println("isInRide(): " + TaxiIstance.getInstance().isInRide());
                            System.out.println("isInElection(): " + TaxiIstance.getInstance().isInElection());
                        } else {
                            TaxiIstance.getInstance().setInElection(true);
                            TaxiIstance.getInstance().setIdRideInElection(ride.getIDRide());

                            ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();
                            int countElection = 0;

                            if (taxiList.size() == 1) { // I'm only the one Taxi in SETA
                                taxiTakesRide(ride);
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
                                                .setX(ride.getStartPosition().getX())
                                                .setY(ride.getStartPosition().getY())
                                                .build();

                                        GrpcServiceOuterClass.RideElectionRequest request = GrpcServiceOuterClass.RideElectionRequest
                                                .newBuilder()
                                                .setIdTaxi(TaxiIstance.getInstance().getMyTaxi().getId())
                                                .setIdRide(ride.getIDRide())
                                                .setStartPositionRide(position)
                                                .setBatteryLevel(TaxiIstance.getInstance().getMyTaxi().getBatteryLevel())
                                                .build();

                                        GrpcServiceOuterClass.RideElectionResponse response;
                                        try {
                                            response = stub.election(request);
                                            System.out.println(response);

                                            if (response.getMessageElection().equals("OK")) {
                                                countElection++;
                                            }
                                        } catch (Exception e) {
                                            System.out.println("ERRORE: " + e.getMessage());
                                            System.out.println("🔴 TaxiPubSub.RideElectionRequest - I can't contact taxi with ID: " + taxi.getId());
                                            TaxiIstance.getInstance().removeTaxi(taxi);
                                        }
                                        channel.shutdownNow();
                                    }
                                }

                                if (countElection == taxiList.size() - 1) { // -1 because in taxiList I'm NOT present
                                    System.out.println("📍 ELECTION for ride " + ride.getIDRide() + " WON by ME -> Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());

                                    taxiTakesRide(ride);
                                } else {
                                    System.out.println("🚕 NOT manage ride " + ride.getIDRide());

                                    synchronized (TaxiIstance.getInstance().getElectionLock()) {
                                        TaxiIstance.getInstance().setInElection(false);
                                        TaxiIstance.getInstance().getElectionLock().notify();
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. connectionLost
                // informs the client of an unexpected disconnection with the broker
                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage() + "-  Thread PID: " + Thread.currentThread().getId());
                }

                // 3. deliveryComplete
                // whether the message was delivered, or not delivered, to the broker
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used here
                }

            });

            client.subscribe(topic, qos);

            System.out.println("🚚 Subscribed to topics : " + topic);

        } catch (MqttException me) {
            System.out.println("🔴 MqttException");
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }


    /*
        1. 5 seconds for ride
        2. I change my data
            2.1 1% of battery for kilometres
        3. RPC to send data to update other Taxis
        4. If I change districts after ride, I subscribe to new topic
        5. setRiding to boolean false
    */
    private void taxiTakesRide(Ride ride) throws MqttException, InterruptedException {
        System.out.println("🗾 I manage RIDE " + ride);
        TaxiIstance.getInstance().setInRide(true);
        TaxiIstance.getInstance().setIdRideOnRoad(ride.getIDRide());

        // notify that I'm not in election anymore
        synchronized (TaxiIstance.getInstance().getElectionLock()) {
            TaxiIstance.getInstance().setInElection(false);
            TaxiIstance.getInstance().getElectionLock().notify();
        }

        MqttMessage payload = new MqttMessage(ride.toJsonString().getBytes()); // getBytes converts message in binary

        // TOPIC example: "seta/smartcity/rides/accomplished/15"
        client.publish("seta/smartcity/rides/accomplished/" + ride.getIDRide(), payload);

        // 5 seconds to manage ride
        Thread.sleep(5000);

        // km for rides
        int kmRide = (int) Math.round(
                Utils.getDistanceBetweenTwoPosition(
                        ride.getStartPosition(),
                        ride.getDestinationPosition())
        );

        final int percentageBatteryUsedForKm = 1;
        int updateBatteryLevel = TaxiIstance.getInstance().getMyTaxi().getBatteryLevel() - kmRide * percentageBatteryUsedForKm;

        System.out.println("🗺 KM Ride: " + kmRide);
        System.out.println("🪫 BATTERY level: " + updateBatteryLevel);

        Position newTaxiPosition = ride.getDestinationPosition();

        // check if batteries is < 30 %
        if (updateBatteryLevel < 30) {
            System.out.println("⚡️ RECHARGE request: batteryLevel < 30%");
            TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_REQUESTED);

            ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();
            int countElection = 0;

            Date date = new Date();
            long timestamp = date.getTime(); // timestamp in ms

            if (taxiList.size() == 1) { // I'm only the one Taxi in SETA
                System.out.println("♻️ 🪫️ RECHARGE station in " + newTaxiPosition.getDistrictByPosition() + " district WON by ME - Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());

                // recharge
                TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_IN_USED);

                System.out.println("⚡️ Charging...");
                Thread.sleep(10000); // 10 seconds
                System.out.println("⚡️ Battery completed...");

                updateBatteryLevel = 100;
                ArrayList<Integer> arr = newTaxiPosition.getPositionOfRechargeStationByDistrict();
                newTaxiPosition = new Position(arr.get(0), arr.get(1));

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
                            }
                        } catch (Exception e) {
                            System.out.println("ERRORE: " + e.getMessage());
                            System.out.println("🔴 TaxiPubSub.taxiTakesRide - I can't contact taxi with ID: " + taxi.getId());
                            TaxiIstance.getInstance().removeTaxi(taxi);
                        }
                        channel.shutdownNow();
                    }
                }

                if (countElection == taxiList.size() - 1) {
                    System.out.println("♻️ 🪫️ RECHARGE station in " + newTaxiPosition.getDistrictByPosition() + " districts WON by ME - Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());

                    // recharge
                    TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_IN_USED);

                    System.out.println("⚡️ Charging...");
                    Thread.sleep(10000); // 10 seconds
                    System.out.println("⚡️ Battery completed...");

                    updateBatteryLevel = 100;
                    ArrayList<Integer> arr = newTaxiPosition.getPositionOfRechargeStationByDistrict();
                    newTaxiPosition = new Position(arr.get(0), arr.get(1)); // management of position because Jersey didn't work using new Position

                    synchronized (TaxiIstance.getInstance().getRechargeLock()) {
                        TaxiIstance.getInstance().setInCharge(TaxiIstance.RechargeStatus.BATTERY_NOT_IN_USED);
                        TaxiIstance.getInstance().getRechargeLock().notify();
                    }
                } else {
                    System.out.println("❌ 🪫 RECHARGE station NOT WON by Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());
                }
            }
        }


        // set new batteryLevel, position, kmTravelled, numberRides
        TaxiIstance.getInstance().getMyTaxi().setBatteryLevel(updateBatteryLevel);
        TaxiIstance.getInstance().getMyTaxi().setPosition(newTaxiPosition);
        TaxiIstance.getInstance().addKmTravelled(kmRide);
        TaxiIstance.getInstance().addNumberRides();


        // update other taxi with my new data
        ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();

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
                            .setIdRide(ride.getIDRide())
                            .setBatteryLevel(updateBatteryLevel)
                            .setFinalPosition(position)
                            .build();

                    GrpcServiceOuterClass.TaxiInfoAfterRideResponse response;
                    try {
                        response = stub.notifyTaxisAfterRide(request);
                    } catch (Exception e) {
                        System.out.println("ERRORE: " + e.getMessage());
                        System.out.println("⚠️ TaxiPubSub.taxiTakesRide - I can't contact taxi with ID: " + taxi.getId());

                        TaxiIstance.getInstance().removeTaxi(taxi);
                    }
                    channel.shutdownNow();
                }
            }
        }

        // I change the district with the district of destination position or recharge station (ONLY if change)
        if (ride.getStartPosition().getDistrictByPosition() != newTaxiPosition.getDistrictByPosition()) {
            client.unsubscribe(topic);
            setDistrictNumber(newTaxiPosition.getDistrictByPosition());

            setTopic(BASE_TOPIC + newTaxiPosition.getDistrictByPosition());
            client.subscribe(topic);

            System.out.println("🗺 NEW district: " + newTaxiPosition.getDistrictByPosition());
        } else {
            System.out.println("🗺 SAME district: " + newTaxiPosition.getDistrictByPosition());
        }

        // setRiding to false
        synchronized (TaxiIstance.getInstance().getRideLock()) {
            TaxiIstance.getInstance().setInRide(false);
            TaxiIstance.getInstance().getRideLock().notify();
        }
        System.out.println("🚕 Ride " + ride.getIDRide() + " FINISHED");


        // I'm free, I advise SETA to public eventually a ride in my district
        String payloadTaxiFree = String.valueOf(newTaxiPosition.getDistrictByPosition());
        MqttMessage messageRide = new MqttMessage(payloadTaxiFree.getBytes());
        messageRide.setQos(2);

        client.publish("seta/smartcity/taxi/free/" + TaxiIstance.getInstance().getMyTaxi().getId(), messageRide);
    }

    public static void disconnectClient() {
        //System.out.println("disconnectClient()");
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.out.println("disconnectClient - Errore: " + e);
        }
        System.out.println("✅ MQTT Client disconnected successfully!");
    }
}
