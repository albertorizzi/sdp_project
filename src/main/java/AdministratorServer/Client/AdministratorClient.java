package AdministratorServer.Client;

import AdministratorServer.Model.Taxi;
import AdministratorServer.Model.Taxis;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class AdministratorClient {
    // TODO: da implementare

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
            System.out.println("\n");

            n = in.nextInt();
            System.out.println("Your selection: " + n); // testing

            switch (n) {
                case 1:
                    System.out.println("1. List of taxis 🚖");

                    Client client = Client.create();
                    String url = "http://localhost:1337/taxi/all";
                    WebResource webResource = client.resource(url);

                    ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

                    if (response.getStatus() == 200) {
                        Gson gson = new Gson();
                        Taxis taxis = gson.fromJson(response.getEntity(String.class), Taxis.class);

                        for (Taxi taxi : taxis.getTaxisList()) {
                            System.out.println(taxi.toString());
                        }
                    } else {
                        System.out.println("Server error: retry");
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
                    break;


                case 3:
                    System.out.println("3. Average statistics of all taxis between timestamp T1 and T2");

                    break;
                default:
                    System.out.println("Selection not valid");
            }


        } while (n != 0);
    }
}
