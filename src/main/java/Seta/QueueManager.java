package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Rides;
import AdministratorServer.Model.Taxi;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;

public class QueueManager extends Thread {
    private static MqttClient client;
    private String clientId;
    private ArrayList<String> subTopicArray;
    private int subQos;
    private String broker;

    public QueueManager(String clientId, ArrayList<String> subTopicArray, int subQos, String broker) {
        this.clientId = clientId;
        this.subTopicArray = subTopicArray;
        this.subQos = subQos;
        this.broker = broker;
    }

    @Override
    public void run() {
        super.run();

        System.out.println("✅ QueueManager Thread started...");

        // MqqtConnection QueueManager
        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connect the client
            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts); // bloccante
            System.out.println(clientId + " Connected");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        try {

            client.setCallback(new MqttCallback() {
                // seta/smartcity/accomplished/ride/#

                // 1. messageArrived
                // ricezione di un msg su uno specifico topic sul quale eravamo sottoscritti
                public void messageArrived(String topic, MqttMessage message) throws JSONException, MqttException {
                    System.out.println(topic);
                    String[] splitTopic = topic.split("/");

                    if (splitTopic[splitTopic.length - 2].equals("accomplished")) {
                        int idRide = Integer.parseInt(splitTopic[splitTopic.length - 1]);

                        System.out.println("Presa in carico corsa " + idRide + " e tolta da rideQueue");
                        System.out.println(Rides.getInstance().getRidesQueue().size());
                        Rides.getInstance().remove(idRide);
                        System.out.println(Rides.getInstance().getRidesQueue().size());
                    } else if (splitTopic[splitTopic.length - 2].equals("free")) {
                        //TODO: quando ricevo uno che è free pubblico la prima della coda e lo ripubblico

                        int idTaxi = Integer.parseInt(splitTopic[splitTopic.length - 1]);


                        System.out.println("Taxi libero: " + idTaxi);
                        System.out.println("Distretto libero: " + message);

                        Ride ride = Rides.getInstance().getRideByDistrictOfStartPosition(Integer.parseInt(String.valueOf(message)));


                        if (ride != null) {
                            System.out.println("ride ottenuta con distretto: " + ride.getStartPosition().getDistrictByPosition());

                            // public ride
                            // send ride to Mqqt to Topic district
                            String payload = ride.toJsonString();

                            message = new MqttMessage(payload.getBytes()); // getBytes converte il msg in binario
                            message.setQos(2);
                            // System.out.println(clientId + " Publishing message: " + payload.toString());
                            // System.out.println("Topic: " + topic + actualDistrict);
                            System.out.println("\n" + "Ride #" + ride.getIDRide() + " REPUBLISHED");
                            System.out.println("🗺 POSITION: start -> " + ride.getStartPosition().getDistrictByPosition() +
                                    ", destination -> " + ride.getDestinationPosition().getDistrictByPosition());
                            client.publish("seta/smartcity/rides/district" + ride.getStartPosition().getDistrictByPosition(), message);

                            Rides.getInstance().add(ride);

                            System.out.println("REPUBLIC RIDE for TOPIC: seta/smartcity/rides/district" + ride.getStartPosition().getDistrictByPosition());
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

            System.out.println("🚚 Subscribed to topics : " + subTopicArray);

        /*    for (String s : subTopicArray) {
                client.subscribe(s, subQos);
            }*/

            client.subscribe(subTopicArray.get(0), subQos);
            client.subscribe(subTopicArray.get(1), subQos);


        } catch (MqttException e) {
            e.printStackTrace();
        }


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
