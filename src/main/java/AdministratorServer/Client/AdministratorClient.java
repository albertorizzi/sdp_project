package AdministratorServer.Client;

import AdministratorServer.Model.Taxi;
import AdministratorServer.Model.Taxis;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class AdministratorClient {
    // TODO: da terminare

    static Client client = Client.create();
    static String url;
    static WebResource webResource;
    static ClientResponse response;

    public static void main(String[] args) {

        int n = 0;
        Scanner in = new Scanner(System.in);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        do {

            System.out.println("\n******************************");
            System.out.println("Welcome to Administrator Client Admin 🖲");
            System.out.println("Select one of the following request:");
            System.out.println("1. List of the taxis");
            System.out.println("2. Average last n statistics of given Taxi");
            System.out.println("3. Average statistics of all taxis between timestamp T1 and T2");
            System.out.println("4. Timestamp");
            System.out.println("\n");

            n = in.nextInt();
            System.out.println("Your selection: " + n); // testing

            switch (n) {
                case 1:
                    System.out.println("1. List of taxis 🚖");

                    url = "http://localhost:1337/taxi/all";
                    webResource = client.resource(url);

                    response = webResource.type("application/json").get(ClientResponse.class);

                    if (response.getStatus() == 200) {
                        Gson gson = new Gson();
                        Taxis taxis = gson.fromJson(response.getEntity(String.class), Taxis.class);

                        for (Taxi taxi : taxis.getTaxisList()) {
                            System.out.println(taxi.toString());
                        }
                    } else {
                        System.out.println("SERVER ERROR: listOfTaxi");
                    }

                    break;
                case 2:
                    int numberStatistic;
                    int idTaxi;
                    do {
                        System.out.println("How many statistics do you want?");
                        numberStatistic = in.nextInt();

                        System.out.println("Digit the ID of Taxi");
                        idTaxi = in.nextInt();
                    } while (numberStatistic <= 0);

                    System.out.println("2. Average last " + numberStatistic + " statistics of Taxi " + idTaxi);

                    // example http://localhost:1337/statistic/1/taxi/992
                    String url = "http://localhost:1337/statistic/" + numberStatistic + "/taxi/" + idTaxi;
                    webResource = client.resource(url);

                    response = webResource.type("application/json").get(ClientResponse.class);

                    if (response.getStatus() == 200 || response.getStatus() == 404) {
                        System.out.println(response.getEntity(String.class));
                    } else {
                        System.out.println("SERVER ERROR: last n statistics");
                    }
                    break;


                case 3:
                    System.out.println("3. Average statistics of all taxis between timestamp T1 and T2");

                    String timestamp1;
                    String timestamp2;
                    boolean digitizing = true;
                    do {
                        System.out.println("Insert T1");
                        timestamp1 = in.next();

                        System.out.println("Insert T2");
                        timestamp2 = in.next();
                        digitizing = false;
                    } while (digitizing);

                    // example http://localhost:1337/statistic/timestamp/000-000
                    url = "http://localhost:1337/statistic/timestamp/" + timestamp1 + "-" + timestamp2;

                    webResource = client.resource(url);

                    response = webResource.type("application/json").get(ClientResponse.class);

                    if (response.getStatus() == 200) {
                        System.out.println(response.getEntity(String.class));
                    } else {
                        System.out.println("SERVER ERROR: last n statistics");
                    }
                    break;

                case 4:
                    System.out.println("4. Timestamp");

                    // example http://localhost:1337/statistic/timestamp
                    url = "http://localhost:1337/statistic/timestamp";

                    webResource = client.resource(url);

                    response = webResource.type("application/json").get(ClientResponse.class);

                    if (response.getStatus() == 200) {
                        System.out.println(response.getEntity(String.class));
                    } else {
                        System.out.println("SERVER ERROR: statistics");
                    }
                    break;

                default:
                    System.out.println("Selection not valid");
            }


        } while (n != 0);
    }
}
