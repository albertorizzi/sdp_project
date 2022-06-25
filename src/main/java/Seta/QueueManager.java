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
                // 1. messageArrived
                // ricezione di un msg su uno specifico topic sul quale eravamo sottoscritti
                public void messageArrived(String topic, MqttMessage message) throws JSONException {
                    String[] splitTopic = topic.split("/");
                    int idRide = Integer.parseInt(splitTopic[splitTopic.length - 2]);


                    if (splitTopic[splitTopic.length - 1].equals("accomplished")) {
                        System.out.println("Presa in carico corsa " + idRide + "e tolta da rideQueue");
                        Rides.getInstance().remove(idRide);
                    } else if (splitTopic[splitTopic.length - 1].equals("unaccomplished")) {
                        System.out.println("unaccomplished " + idRide);
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

            for (String s : subTopicArray) {
                client.subscribe(s, subQos);
            }


        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public static void disconnectClient(){
        //System.out.println("disconnectClient()");
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.out.println("disconnectClient - Errore: " + e);
        }
        System.out.println("✅ MQTT Client disconnected successfully!");
    }
}
