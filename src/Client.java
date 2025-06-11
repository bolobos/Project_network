import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket client = null;
    private String ipPublic = "192.168.91.155"; // Adresse IP publique du serveur
    private int port_server = 9081; // Port du serveur

    private String name;
    private String id;

    // Constructeur par défaut
    public Client() {}

    // Constructeur avec deux arguments
    public Client(String name, String id) {
        this.name = name;
        this.id = id;
    }

    // Getters et setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Client{name='" + name + "', id='" + id + "'}";
    }

    public void listenSocket(String clientName, String clientId) {
        try {
            // Créer le client
            client = new Socket(this.ipPublic, port_server);
            System.out.println("Client connected to server at " + ipPublic + ":" + port_server);

            // Actions lors de l'arrêt du client
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down client...");
                try {
                    if (client != null && !client.isClosed()) {
                        client.close();
                        System.out.println("Client socket closed.");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }));

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            // Envoyer les informations du client au serveur
            out.println("CLIENT:" + clientName + "," + clientId);
            System.out.println("Client information sent to server.");

            // Thread pour recevoir les messages du serveur
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("SERVER: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Error reading from server: " + e.getMessage());
                }
            });
            receiveThread.start();

            // Envoyer des messages personnalisés
            System.out.println("Type your messages below (type 'exit' to quit):");
            while (true) {
                System.out.print("Enter your message: ");
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting client...");
                    break;
                }
                out.println(message);
                System.out.println("CLIENT - DATA SENT: " + message);
            }

            // Fermer les ressources
            scanner.close();
            client.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(-1);
        }
    }

    // Main method to execute the client
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <clientName> <clientId>");
            return;
        }

        String clientName = args[0];
        String clientId = args[1];

        System.out.println("Starting client: " + clientName + " (ID: " + clientId + ")");
        Client client = new Client();
        client.listenSocket(clientName, clientId);
    }
}