import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Symbolise un client / 
public class Client {

    private Socket socket = null;
    private String ip = ""; // Adresse IP publique du serveur
    private int port_server = 9081; // Port du serveur
    private String serveur_cible = "XXX.XXX.XXX.XXX";
    private String client_cible = "XXX.XXX.XXX.XXX";

    private String name;
    private String id;

    // Constructeur par défaut
    public Client() {
    }

    // Constructeur avec deux arguments
    public Client(String name, String id) {
        this.name = name;
        this.id = id;

        LocalIP localIP = new LocalIP();
        this.ip = localIP.getLocalIP();
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
            socket = new Socket(this.ip, port_server);
            System.out.println("Client connected to server at " + this + ":" + port_server);

            // Actions lors de l'arrêt du client
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down client...");
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        System.out.println("Client socket closed.");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }));

            LocalIP localIP = new LocalIP();
            Trame_message trame_message = new Trame_message(0, serveur_cible, localIP.getServerLocalIP(serveur_cible),
                    client_cible, localIP.getLocalIP(), "Bonjour");
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());

            // Thread pour recevoir les messages du serveur
            Thread receiveThread = new Thread(() -> {
                try {
                    ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
                    while (true) {
                        Object obj = objectIn.readObject();
                        if (obj instanceof Trame_message) {
                            Trame_message trame = (Trame_message) obj;
                            System.out.println("SERVER: " + trame.getDu());
                        } else {
                            System.out.println("Received unknown object from server.");
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error reading object from server: " + e.getMessage());
                }
            });
            receiveThread.start();

            // Envoyer des messages personnalisés
            System.out.println("Type your messages below (type 'exit' to quit):");
            while (true) {
                System.out.print("Enter a text to send the object :");
                Scanner scanner = new Scanner(System.in);
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting client...");
                    break;
                }
                objectOut.writeObject(trame_message);
                objectOut.flush();
                System.out.println("Message envoyé.");
                System.out.println("CLIENT - DATA SENT: " + trame_message.getDu());
                scanner.close();
            }

            // Fermer les ressources
            
            socket.close();

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