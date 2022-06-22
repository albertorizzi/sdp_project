package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import com.sun.javafx.scene.traversal.SubSceneTraversalEngine;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.Scanner;

public class TaxiSubscriber extends Thread {
    private static MqttClient client;
    private int districtNumber;
    private String BASE_TOPIC = "seta/smartcity/rides/district";
    private String topic;
    private String broker = "tcp://localhost:1883";


    public TaxiSubscriber(int districtNumber) {
        this.districtNumber = districtNumber;
        this.topic = BASE_TOPIC + districtNumber;
    }

    public void run() {
        System.out.println("🚚 Starting Broker MQTT...");

        String clientId = MqttClient.generateClientId();
        int qos = 2;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);


            client.connect(connOpts); //sincrono

            client.setCallback(new MqttCallback() {
                // 1. messageArrived
                // ricezione di un msg su uno specifico topic sul quale eravamo sottoscritti
                public void messageArrived(String topic, MqttMessage message) throws JSONException {

                    // Called when a message arrives from the server that matches any subscription made by the client
                    //String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload()); // da binario a stringa

                    System.out.println(receivedMessage);


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
                    System.out.println(ride);


                    ArrayList<Taxi> taxiList = TaxiIstance.getInstance().getTaxiList();


                    for (Taxi taxi : taxiList) {
                        if (!taxi.isInCharge() || !taxi.isInRide()) {
                            if (taxi.getId() != TaxiIstance.getInstance().getMyTaxi().getId()) {
                                //opening a connection with the drone's server
                                final ManagedChannel channel = ManagedChannelBuilder
                                        .forTarget(taxi.getAddressServerAdministrator() + ":" + taxi.getPortNumber())
                                        .usePlaintext()
                                        .build();

                                GrpcServiceGrpc.GrpcServiceBlockingStub stub = GrpcServiceGrpc.newBlockingStub(channel);

                                GrpcServiceOuterClass.RideElectionRequest request = GrpcServiceOuterClass.RideElectionRequest
                                        .newBuilder()
                                        .setIdTaxi(taxi.getId())
                                        .setIdRide(ride.getIDRide())
                                        .build();
                                System.out.println("[GRPC CLIENT] request: " + request);

                                GrpcServiceOuterClass.RideElectionResponse response;
                                try {
                                    response = stub.election(request);
                                    //System.out.println("[GRPC CLIENT] response frome drone " + drone.getId() + ": " + response.getId());
                                    System.out.println(response);


                                } catch (Exception e) {

                                    //System.out.println("ERRORE: " + e.getMessage());
                                    System.out.println("🔴 welcomeClient - Non riesco a contattare il drone " + taxi.getId());

                                    // TODO: remove taxi if this doesn't response
                                    //TaxiIstance.getInstance().remove(tavi)

                                }

                                channel.shutdownNow();
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


}
