package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import Utils.Utils;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;

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

            client.connect(connOpts); //sincrono

            client.setCallback(new MqttCallback() {
                // messageArrived: reception of message of specific topic on this there's a subscription
                public void messageArrived(String topic, MqttMessage message) throws JSONException, MqttException, InterruptedException {
                    // Called when a message arrives from the server that matches any subscription made by the client
                    //String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload()); // from binary to string

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject = new JSONObject(receivedMessage);
                    } catch (JSONException err) {
                        err.printStackTrace();
                    }

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
                    System.out.println("\n" + "📍 New Ride -> id: " + ride.getIDRide() + " district: " + ride.getStartPosition().getDistrictByPosition());

                    // check if I'm riding or I'm busy in a ride
                    // TODO: add check if I would to recharge battery
                    if (TaxiIstance.getInstance().getMyTaxi().isInCharge() || TaxiIstance.getInstance().getMyTaxi().isInRide()) {
                        System.out.println("NON gestisco la corsa (sono impegnato) " + ride); // TODO: cosa devo fare?
                    } else {
                        ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();
                        int countElection = 0;

                        if (taxiList.size() == 1) { // I'm only the one Taxi in SETA
                            taxiTakesRide(ride);
                        } else {
                            for (Taxi taxi : taxiList) {
                                if (!taxi.isInCharge() || !taxi.isInRide()) {
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
                                                .setIdTaxi(taxi.getId())
                                                .setIdRide(ride.getIDRide())
                                                .setStartPositionRide(position)
                                                .build();

                                        GrpcServiceOuterClass.RideElectionResponse response;
                                        try {
                                            response = stub.election(request);

                                            if (response.getMessageElection().equals("OK")) {
                                                countElection++;
                                            } else if (response.getMessageElection().equals("NO")) {
                                                System.out.println("🚕 NOT manage ride " + ride.getIDRide());
                                                break;
                                            }

                                            if (countElection == taxiList.size() - 1) {
                                                System.out.println("📍 ELECTION for ride " + ride.getIDRide() + " WON by Taxi " + TaxiIstance.getInstance().getMyTaxi().getId());

                                                taxiTakesRide(ride);
                                            }
                                        } catch (Exception e) {
                                            //System.out.println("ERRORE: " + e.getMessage());
                                            System.out.println("🔴 welcomeClient - Non riesco a contattare il drone " + taxi.getId());
                                            TaxiIstance.getInstance().removeTaxi(taxi);
                                        }
                                        channel.shutdownNow();
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. connectionLost
                // informa il client di una disconnessione inaspettata con il broker
                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage() + "-  Thread PID: " + Thread.currentThread().getId());
                }

                // 3. deliveryComplete
                // se il msg è stato consegnato, o non è stato consegnato, al broker
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used here
                }

            });

            //System.out.println(clientId + " Subscribing ... - Thread PID: " + Thread.currentThread().getId());
            client.subscribe(topic, qos);
            // posso anche usare i vettori: .subscribre(["topic/a","topic/b"],[qos_a,qos_b])
            System.out.println("🚚 Subscribed to topics : " + topic);


        } catch (MqttException me) {
            System.out.println("🔴 MqttException");
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } // catch

    }


    /*
        1. 5 secondi per ride
        2. Cambio i miei dati
            2.1 1% di batteria per ogni chilometro
        3. RCP per inviare i miei nuovi dati a tutti gli altri taxi
        4. Mi iscrivo ad un altro topic, quello del mio nuovo distretto
        5. setRiding a false
    */
    private void taxiTakesRide(Ride ride) throws MqttException, InterruptedException {
        //  System.out.println("Gestisco io la corsa" + ride);
        TaxiIstance.getInstance().getMyTaxi().setInRide(true);

        MqttMessage payload = new MqttMessage(ride.toJsonString().getBytes()); // getBytes converts message in binary

        // TOPIC example: "seta/smartcity/rides/15/accomplished"
        client.publish("seta/smartcity/rides/" + ride.getIDRide() + "/accomplished", payload);

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

        // set new batteryLevel
        TaxiIstance.getInstance().getMyTaxi().setBatteryLevel(updateBatteryLevel);

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
                            .setX(ride.getDestinationPosition().getX())
                            .setY(ride.getDestinationPosition().getY())
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
                        //System.out.println(response);
                    } catch (Exception e) {
                        System.out.println("⚠️ TaxiPubSub.taxiTakesRide - I can't contact taxi with ID: " + taxi.getId());

                        TaxiIstance.getInstance().removeTaxi(taxi);
                    }
                    channel.shutdownNow();
                }

            }
        }

        // I change the district with the district of destination position (ONLY if change)
        if (ride.getStartPosition().getDistrictByPosition() != ride.getDestinationPosition().getDistrictByPosition()) {
            client.unsubscribe(topic);
            setDistrictNumber(ride.getDestinationPosition().getDistrictByPosition());

            setTopic(BASE_TOPIC + ride.getDestinationPosition().getDistrictByPosition());
            client.subscribe(topic);

            System.out.println("🗺 NEW district: " + ride.getDestinationPosition().getDistrictByPosition());
        } else {
            System.out.println("🗺 SAME district: " + ride.getDestinationPosition().getDistrictByPosition());
        }

        // setRiding to false
        TaxiIstance.getInstance().getMyTaxi().setInRide(false);
        System.out.println("🚕 Ride FINISHED");
    }
}
