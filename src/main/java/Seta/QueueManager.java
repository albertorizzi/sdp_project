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
            connOpts.setMaxInflight(500);
            connOpts.setConnectionTimeout(0);

            // Connect the client
            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts); // bloccante
            System.out.println(clientId + " Connected");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        try {

            client.setCallback(new MqttCallback() {
                // seta/smartcity/ride/accomplished/#
                // seta/smartcity/ride/free/#

                // messageArrived: reception of message of specific topic on this there's a subscription
                public void messageArrived(String topic, MqttMessage message) throws JSONException, MqttException {
                    System.out.println(topic);
                    String[] splitTopic = topic.split("/");

                    if (splitTopic[splitTopic.length - 2].equals("accomplished")) {
                        int idRide = Integer.parseInt(splitTopic[splitTopic.length - 1]);
                        Rides.getInstance().remove(idRide);

                        System.out.println("🖲 TAKE in charge ride " + idRide + " and removed from queue");
                    } else if (splitTopic[splitTopic.length - 2].equals("free")) {
                        // when taxi is free, take the first ride and public on the topic of taxi position

                        int idTaxi = Integer.parseInt(splitTopic[splitTopic.length - 1]);


                        System.out.println("🚖 Taxi FREE: " + idTaxi);
                        System.out.println("🗺 District FREE: " + message);

                        Ride ride = Rides.getInstance().getRideByDistrictOfStartPosition(Integer.parseInt(String.valueOf(message)));

                        if (ride != null) {
                            // public ride
                            // send ride to Mqqt to Topic district
                            String payload = ride.toJsonString();

                            message = new MqttMessage(payload.getBytes()); // getBytes converte il msg in binario
                            message.setQos(2);

                            System.out.println("\n" + "Ride #" + ride.getIDRide() + " REPUBLISHED");
                            System.out.println("🗺 POSITION: start -> " + ride.getStartPosition().getDistrictByPosition() +
                                    ", destination -> " + ride.getDestinationPosition().getDistrictByPosition());
                            client.publish("seta/smartcity/rides/district" + ride.getStartPosition().getDistrictByPosition(), message);

                            Rides.getInstance().add(ride);
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

            client.subscribe(subTopicArray.get(0), subQos);
            client.subscribe(subTopicArray.get(1), subQos);
            System.out.println("🚚 Subscribed to topics : " + subTopicArray);
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
