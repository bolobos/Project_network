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
    private String ip = "192.168.27.50"; // Adresse IP publique du serveur
    private int port_server = 9081; // Port du serveur
    private String serveur_cible = "192.168.27.50";
    private String client_cible = "XXX.XXX.XXX.XXX";

    private String name;
    private String id;

    // Constructeur par défaut
    public Client() {
    }

    // Constructeur avec deux arguments
    public Client(String name, String id, String ipServ) {
        this.name = name;
        this.id = id;
        this.ip = ipServ;

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

    public void listenSocket(String clientName, String clientId, String ipServ) {

        Trame_message disconnectMessage = new Trame_message(
                1, // Par exemple, 1 pour indiquer une déconnexion
                ipServ,
                ipServ,
                null,
                new LocalIP().getLocalIP(),
                "Déconnexion");

        try {

            // Créer le client
            socket = new Socket(ipServ, port_server);
            System.out.println("Client connected to server at " + ipServ + ":" + port_server);

            
            // Actions lors de l'arrêt du client
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down client...");
                try {
                    if (socket != null && !socket.isClosed()) {

                        sendMessage(disconnectMessage, ipServ);

                        socket.close();
                        System.out.println("Client socket closed.");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }));

            LocalIP localIP = new LocalIP();

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
                System.out.print("Type exit if you want to exit. Else, type ENTER to send message");
                Scanner scanner = new Scanner(System.in);
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Existing client...");
                    break;
                }

                System.out.print("Entrez le client cible: ");
                String clientCible = scanner.nextLine();

                System.out.print("Entrez l'IP serveur cible: ");
                String ipServeur = scanner.nextLine();

                System.out.print("Entrez le message: ");
                String contenu = scanner.nextLine();


                Trame_message trame_message = new Trame_message(
                        1,
                        ipServeur,
                        ipServ,
                        clientCible,
                        new LocalIP().getLocalIP(),
                        contenu);

                sendMessage(trame_message, ipServeur);

            }

            // Fermer les ressources

            if (socket != null && !socket.isClosed()) {

                sendMessage(disconnectMessage, serveur_cible);

                socket.close();
                System.out.println("Client socket closed.");
            }

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
        String ipServ = args[2];

        System.out.println("Starting client: " + clientName + " (ID: " + clientId + ")");
        System.out.println("Connecté à l'IP serveur : " + ipServ);
        Client client = new Client();
        client.listenSocket(clientName, clientId, ipServ);
    }

    public void sendMessage(Trame trame, String ipCible) {
        try {
            Socket socket = new Socket(ipCible, 9081);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(trame);
            out.flush();
            socket.close();
            System.out.println("Trame envoyée au client " + ipCible);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'envoi de la trame au serveur " + ipCible + " : " + e.getMessage());
        }
    }
}