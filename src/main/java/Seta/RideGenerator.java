package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RideGenerator extends Thread {
    private MqttClient client;
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

        System.out.println("Thread RideGenerartor started...");

        // MqqtConnection
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

        // Ride Generator
        // every 5 seconds generate one ride starting at random position

        while (true) {
            idRide++;
            try {
                // TODO: generare 2 richieste
                System.out.println("\n" + clientId + " Order #" + idRide);

                int id = idRide;
                int min = 0, max = 9;
                startPosition = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);
                endPosition = new Position((int) (Math.random() * ((max - min) + 1)) + min, (int) (Math.random() * ((max - min) + 1)) + min);

                // TODO: check if actual position isn't equal to endPosition

                actualDistrict = startPosition.getDistrictByPosition();
                ride = new Ride(id, startPosition, endPosition);
                String payload = ride.toJsonString();

                //topic = topic + actualDistrict;

                MqttMessage message = new MqttMessage(payload.getBytes()); // getBytes converte il msg in binario
                message.setQos(qos);
                System.out.println(clientId + " Publishing message: " + payload.toString());
                System.out.println("Topic: " + topic + actualDistrict);
                client.publish(topic + actualDistrict, message);
                System.out.println(clientId + " Message published");

                Thread.sleep(2500); //TODO: cambiar ein 5000

            } catch (MqttException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
