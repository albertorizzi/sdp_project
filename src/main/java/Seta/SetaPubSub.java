package Seta;

import AdministratorServer.Model.Position;
import AdministratorServer.Model.Ride;
import AdministratorServer.Model.Taxi;
import com.example.taxis.GrpcServiceGrpc;
import com.example.taxis.GrpcServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;

public class SetaPubSub {
    /*
    BEFORE starting SetaPublisher, start Mosquito
    brew services start mosquitto
    brew services stop mosquitto
    */

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientIdPub = MqttClient.generateClientId();
        String clientIdSub = MqttClient.generateClientId();


        // publisher
        String baseTopic = "seta/smartcity/rides/district";
        int qos = 2;

        //subscriber
        ArrayList<String> subTopicArray = new ArrayList<String>() {{
            add("seta/smartcity/rides/*/accomplished");
            add("seta/smartcity/rides/*/unaccomplished");
        }};
        int subQos = 2;

        // subscriber and publisher (only if there is a ride in pending)
        QueueManager queueManager = new QueueManager(clientIdSub, subTopicArray, subQos, broker);
        queueManager.start();

        // publisher
        RideGenerator rideGenerator = new RideGenerator(clientIdPub, baseTopic, qos, broker);
        rideGenerator.start();
    }
}
