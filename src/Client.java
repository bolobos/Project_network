import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Quoicoubeh
public class Client { 

    // Object server // Main component who represent the server as a socket
    public Socket client = null;

    private String ipPublic = "130.190.80.223";

    private int port_server = 9081;

    public void listenSocket() {

        try {
            // Create the client
            Socket client = new Socket(this.ipPublic, port_server);
            PublicIP publicIP = new PublicIP();
            System.out.println("Client create at " + port_server + " port with the IP : " + publicIP.getPublicIP());

            // Actions when stopping the client
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                try {
                    if (client != null && !client.isClosed()) {
                        client.close(); // close the listening socket
                        System.out.println("Client socket closed.");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }));

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // Send one message
            while (true) {

                String message = "Bonjour";
                    
                out.println(message);
                System.out.println("CLIENT - DATA SEND");

                // Delay 2 secondes
                try {
                    Thread.sleep(2000); // 2000 millisecondes = 2 secondes
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break; // Optionally exit the loop
                }
            }

        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
            System.exit(-1);
        }

    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public int getPort_server() {
        return port_server;
    }

    public void setPort_server(int port_server) {
        this.port_server = port_server;
    }
}
