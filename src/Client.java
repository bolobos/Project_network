import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Classe qui symbolise un client dans le réseau
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

    // Constructeur avec nom, id et IP serveur
    public Client(String name, String id, String ipServ) {
        this.name = name;
        this.id = id;
        this.ip = ipServ;

        LocalIP localIP = new LocalIP();
        this.ip = localIP.getLocalIP();
    }

    // Getters et setters pour le nom et l'id du client
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

    /**
     * Méthode principale pour écouter le socket et gérer la communication avec le
     * serveur.
     * 
     * @param clientName Nom du client
     * @param clientId   Id du client
     * @param ipServ     Adresse IP du serveur
     */
    public void listenSocket(String clientName, String clientId, String ipServ) {

        // Message de déconnexion à envoyer lors de l'arrêt du client
        Trame_message disconnectMessage = new Trame_message(
                1, // Par exemple, 1 pour indiquer une déconnexion
                ipServ,
                ipServ,
                null,
                new LocalIP().getLocalIP(),
                "Déconnexion");

        try {

            // Création du socket client
            socket = new Socket(ipServ, port_server);
            System.out.println("Client connected to server at " + ipServ + ":" + port_server);

            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectOut.writeObject(clientName); // Envoie le nom du client au serveur
            objectOut.flush();

            // Ajout d'un hook pour gérer la fermeture propre du client
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down client...");
                try {
                    if (socket != null && !socket.isClosed()) {

                        objectOut.writeObject(disconnectMessage);
                        objectOut.flush();

                        socket.close();
                        System.out.println("Client socket closed.");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }));

            LocalIP localIP = new LocalIP();

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

            // Boucle pour envoyer des messages personnalisés à d'autres clients
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

                System.out.print("Entrez le message: ");
                String contenu = scanner.nextLine();

                // Création de la trame à envoyer
                Trame_message trame_message = new Trame_message(
                        1,
                        null,
                        ipServ,
                        clientCible,
                        new LocalIP().getLocalIP(),
                        contenu);

                objectOut.writeObject(trame_message);
                objectOut.flush();

            }

            // Fermeture des ressources à la fin
            if (socket != null && !socket.isClosed()) {

                objectOut.writeObject(disconnectMessage);
                objectOut.flush();

                socket.close();
                System.out.println("Client socket closed.");
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Point d'entrée principal du client.
     * 
     * @param args Arguments de la ligne de commande : nom, id, ip serveur
     */
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
}