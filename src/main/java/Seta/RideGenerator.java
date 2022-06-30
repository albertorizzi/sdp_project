package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Rides;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RideGenerator extends Thread {
    private static MqttClient client;
    private String clientId;
    private String topic;
    private int qos;
    private String broker;

    private Ride ride;
    private Integer idRide = 0;
    private Position startPosition;
    private Position endPosition;
    private int actualDistrict;

    public RideGenerator(String clientId, String topic, int qos, String broker) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.broker = broker;
    }

    @Override
    public void run() {
        super.run();

        System.out.println("✅ RideGenerartor Thread started...");

        // MqqtConnection RideGenerator
        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setCleanSession(true);
            connOpts.setMaxInflight(200);
            connOpts.setConnectionTimeout(0);

            // Connect the client
            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts); // bloccante
            System.out.println(clientId + " Connected");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        // Ride Generator
        // every 5 seconds generate 2 rides starting at random position
        // TODO: cambiare in 2

        while (true) {
            idRide++; // idRide incremental
            try {
                System.out.println("\n" + "Ride #" + idRide);

                int id = idRide;
                int min = 0, max = 9;

                /*
                startPosition = new Position(0,3);
                endPosition = new Position(0,4);

                */


                // TODO: debug decommentare

                startPosition = null;
                endPosition = null;

                while (startPosition == endPosition) {
                    startPosition = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                    endPosition = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                }



                actualDistrict = startPosition.getDistrictByPosition();
                ride = new Ride(id, startPosition, endPosition);

                // add ride to RidesQueue
                Rides.getInstance().add(ride);

                // send ride to Mqqt to Topic district
                String payload = ride.toJsonString();

                MqttMessage message = new MqttMessage(payload.getBytes()); // getBytes converte il msg in binario
                message.setQos(qos);
                // System.out.println(clientId + " Publishing message: " + payload.toString());
                // System.out.println("Topic: " + topic + actualDistrict);
                System.out.println("🗺 POSITION: start -> " + actualDistrict + ", destination -> " + ride.getDestinationPosition().getDistrictByPosition());
                client.publish(topic + actualDistrict, message);

                Thread.sleep(5000); // TODO: check 5000
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
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
