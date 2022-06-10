package Seta;

import org.eclipse.paho.client.mqttv3.MqttClient;

/*TERMINALE
brew services start mosquitto
brew services stop mosquitto
*/

public class SetaPublisher {
    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String baseTopic = "seta/smartcity/rides/district";
        int qos = 2;

        RideGenerator rideGenerator = new RideGenerator(clientId, baseTopic, qos, broker);
        rideGenerator.start();
    }
}
