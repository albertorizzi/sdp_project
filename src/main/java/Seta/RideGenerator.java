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
    private Ride ride2;
    private Integer idRide = 1;
    private Position startPosition;
    private Position endPosition;
    private int actualDistrict;

    private Position startPosition2;
    private Position endPosition2;
    private int actualDistrict2;

    public RideGenerator(String clientId, String topic, int qos, String broker) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.broker = broker;
    }

    @Override
    public void run() {
        super.run();

        System.out.println("✅ RideGenerator Thread started...");

        // MqqtConnection RideGenerator
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

        // Ride Generator
        // every 5 seconds generate 2 rides starting at random position
        while (true) {
            try {

                int id = idRide;
                int id2 = idRide + 1;

                int min = 0, max = 9;

                /* test - district 1
                startPosition = new Position(0,3);
                endPosition = new Position(0,4);
                */

                startPosition = null;
                endPosition = null;

                startPosition2 = null;
                endPosition2 = null;

                while (startPosition == endPosition) {
                    startPosition = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                    endPosition = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                }

                while (startPosition2 == endPosition2) {
                    startPosition2 = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                    endPosition2 = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                }

                actualDistrict = startPosition.getDistrictByPosition();
                actualDistrict2 = startPosition2.getDistrictByPosition();

                ride = new Ride(id, startPosition, endPosition);
                ride2 = new Ride(id2, startPosition2, endPosition2);

                // add ride to RidesQueue
                Rides.getInstance().add(ride);
                Rides.getInstance().add(ride2);


                // send ride to Mqqt to Topic district
                String payload = ride.toJsonString();
                String payload2 = ride2.toJsonString();

                MqttMessage message = new MqttMessage(payload.getBytes()); // getBytes converte il msg in binario
                message.setQos(qos);
                MqttMessage message2 = new MqttMessage(payload2.getBytes()); // getBytes converte il msg in binario
                message2.setQos(qos);


                System.out.println("\n" + "Ride #" + id);
                System.out.println("🗺 POSITION: start -> " + actualDistrict + ", destination -> " + ride.getDestinationPosition().getDistrictByPosition());
                client.publish(topic + actualDistrict, message);

                System.out.println("\n" + "Ride #" + id2);
                System.out.println("🗺 POSITION: start -> " + actualDistrict2 + ", destination -> " + ride2.getDestinationPosition().getDistrictByPosition());
                client.publish(topic + actualDistrict2, message);

                Thread.sleep(5000); // TODO: check 5000

                idRide += 2; // idRide incremental
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
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
