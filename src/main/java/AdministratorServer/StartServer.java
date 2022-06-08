package AdministratorServer;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class StartServer {
    private static final String HOST = "localhost";
    private static final int PORT = 1337;


    public static void main(String[] args) throws IOException {
        String URI = "http://" + HOST + ":" + PORT + "/";

        HttpServer server = HttpServerFactory.create(URI);
        server.start();

        System.out.println("Server running!");
        System.out.println("Server started on: " + URI);

        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        server.stop(0);
        System.out.println("Server stopped");
    }
}