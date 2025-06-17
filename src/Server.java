import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe principale représentant un serveur du réseau.
 * Gère la création du serveur, l'initialisation de la table de routage,
 * la réception et le traitement des messages, ainsi que la propagation des
 * tables de routage.
 */
public class Server {

    /** Socket serveur pour accepter les connexions entrantes. */
    private ServerSocket server = null;

    private final Map<String, Socket> clientNameToSocket = new ConcurrentHashMap<>();

    /** Port d'écoute du serveur. */
    private int port_server = 9081;

    /** Adresse IP locale du serveur. */
    private String ipLocal;

    /** Indique si ce serveur est le serveur principal (initiateur du routage). */
    private boolean servPrincipal = false;

    /** Table de routage du serveur. */
    private Table_routage rootingTable = new Table_routage();

    /** États possibles du serveur. */
    public enum stateServer {
        INIT,
        RUNNING,
        STOPPED
    }

    /** État courant du serveur. */
    private stateServer state = stateServer.INIT;

    /**
     * Boucle principale du serveur.
     * Gère l'initialisation, l'écoute des connexions et l'arrêt du serveur.
     */
    public void listenSocket() {
        while (true) {
            switch (state) {
                case INIT:
                    try {
                        // Création du serveur et récupération de l'IP locale
                        server = new ServerSocket(port_server);
                        LocalIP localIP = new LocalIP();
                        ipLocal = localIP.getLocalIP();
                        System.out.println("Server created at port " + port_server + " with IP: " + this.ipLocal);

                        // Ajout d'un hook pour gérer l'arrêt propre du serveur
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            System.out.println("Shutting down server...");
                            state = stateServer.STOPPED;
                        }));

                        // Initialisation du réseau et de la table de routage
                        initNetwork();

                        // Passage à l'état RUNNING
                        state = stateServer.RUNNING;

                    } catch (IOException e) {
                        System.out.println("Erreur : " + e.getMessage());
                        state = stateServer.STOPPED;
                    }
                    break;
                case RUNNING:
                    try {
                        // Attente et gestion des connexions entrantes (clients ou serveurs)
                        System.out.println("En attente de réception...");
                        Socket client = server.accept();
                        ObjectInputStream objectIn = new ObjectInputStream(client.getInputStream());

                        // Récupère le nom du client (par exemple, le client envoie son nom en premier)
                        String clientName = null;
                        try {
                            clientName = (String) objectIn.readObject();
                            clientNameToSocket.put(clientName, client);
                            System.out.println("Client connecté : " + clientName);
                        } catch (ClassNotFoundException e) {
                            System.out.println("Erreur lors de la lecture du nom du client : " + e.getMessage());
                        }

                        try {
                            Object receivedObject = objectIn.readObject();

                            if (receivedObject instanceof Trame_message) {
                                Trame_message receivedMessage = (Trame_message) receivedObject;

                                // Affichage des informations du message reçu
                                System.out.println("Message reçu : " + receivedMessage.getDu());
                                System.out.println("Depuis : " + receivedMessage.getServeur_source());
                                System.out.println("Pour le client : " + receivedMessage.getClient_cible());

                                int res = receivedMessage.redirectMessage(receivedMessage, rootingTable, ipLocal);

                                // Redirection du message selon le code retourné
                                switch (res) {
                                    case 1:
                                        System.out.println("Message destiné au serveur courant.");
                                        // Traiter le message ici si besoin
                                        break;
                                    case 2:
                                        System.out.println("Message destiné à un client local.");
                                        handleTrameMessage(receivedMessage, res);
                                        break;
                                    case 3:
                                        System.out.println(
                                                "Message destiné à un autre serveur (client cible non local).");
                                        handleTrameMessage(receivedMessage, res);
                                        break;
                                    case 4:
                                        System.out.println(
                                                "Message destiné à un autre serveur (serveur cible différent).");
                                        handleTrameMessage(receivedMessage, res);
                                        break;
                                    default:
                                        System.out.println("Code de redirection inconnu : " + res);
                                        break;
                                }
                            }

                            client.close();
                        } catch (IOException | ClassNotFoundException e) {
                            System.out.println("Error handling connection: " + e.getMessage());
                        }

                    } catch (IOException e) {
                        System.out.println("Erreur : " + e.getMessage());
                        state = stateServer.STOPPED;
                    }
                    break;
                case STOPPED:
                    // Fermeture propre du serveur
                    if (server != null && !server.isClosed()) {
                        try {
                            server.close();
                        } catch (IOException ex) {
                            System.out.println("Erreur lors de la fermeture du serveur : " + ex.getMessage());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Traite une trame de message reçue selon le code de redirection.
     * 
     * @param trame La trame de message à traiter.
     * @param res   Le code de redirection déterminé par redirectMessage.
     */
    public void handleTrameMessage(Trame_message trame, int res) {
        String client_cible = trame.getClient_cible(); // Récupère le nom du client cible

        switch (res) {
            case 2:
                // Message destiné à un client local
                for (int i = 0; i < rootingTable.getClients_serveurs().size(); i++) {
                    ArrayList<String> clients = rootingTable.getClients_serveurs().get(i);
                    if (clients.contains(client_cible)) { // Vérifie si le client est local
                        Socket socket = clientNameToSocket.get(client_cible); // Récupère le socket du client
                        if (socket != null && !socket.isClosed()) {
                            try {
                                // Envoie la trame au client via son socket
                                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                                out.writeObject(trame);
                                out.flush();
                                System.out.println("Message envoyé au client local : " + client_cible);
                            } catch (IOException e) {
                                System.out.println("Erreur lors de l'envoi de la trame au client " + client_cible + " : " + e.getMessage());
                            }
                            return; // Message envoyé, on quitte la méthode
                        } else {
                            System.out.println("Socket non trouvée ou fermée pour le client : " + client_cible);
                        }
                    }
                }
                // Si aucun client local trouvé
                System.out.println("Client local introuvable : " + client_cible);
                break;

            case 3:
            case 4:
                // Message destiné à un autre serveur (client cible non local ou serveur cible différent)
                for (int i = 0; i < rootingTable.getClients_serveurs().size(); i++) {
                    ArrayList<String> clients = rootingTable.getClients_serveurs().get(i);
                    if (clients.contains(client_cible)) { // Cherche le serveur qui gère ce client
                        String passerelle = rootingTable.getPasserelles().get(i).getHostAddress(); // Récupère l'IP de la passerelle
                        sendMessage(trame, passerelle); // Relaye la trame vers la passerelle
                        System.out.println(
                                "Message relayé à la passerelle : " + passerelle + " pour client " + client_cible);
                        return; // Message relayé, on quitte la méthode
                    }
                }
                // Si aucune passerelle trouvée pour ce client
                System.out.println("Passerelle introuvable pour client : " + client_cible);
                break;

            default:
                // Cas où le code de redirection n'est pas géré
                System.out.println("Aucune action pour ce code de redirection : " + res);
                break;
        }
    }

    /**
     * Envoie une trame à une IP cible via un socket TCP.
     * 
     * @param trame   La trame à envoyer.
     * @param ipCible L'adresse IP de destination.
     */
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

    /**
     * Initialise la table de routage à partir des entrées utilisateur.
     * 
     * @return Une trame de routage initialisée.
     */
    public Trame_routage initTrame() {
        Trame_routage trame = null;

        try {
            // Création d'un menu pour entrer les IP des serveurs et les clients associés
            ArrayList<String> serveurs = new ArrayList<>();
            ArrayList<Inet4Address> passerelles = new ArrayList<>();
            ArrayList<ArrayList<String>> clients_serveurs = new ArrayList<>();
            ArrayList<Integer> distance = new ArrayList<>();

            Scanner scanner = new Scanner(System.in);

            System.out.print("Ce serveur est-il le serveur principal ? (oui/non) : ");
            String principalInput = scanner.nextLine().trim().toLowerCase();
            servPrincipal = principalInput.equals("oui") || principalInput.equals("o") || principalInput.equals("yes");

            System.out.print("Combien de serveurs voulez-vous ajouter ? ");
            int nbServeurs = Integer.parseInt(scanner.nextLine());

            for (int i = 0; i < nbServeurs; i++) {
                System.out.print("Entrez l'IP du serveur #" + (i + 1) + " : ");
                String ipServeur = scanner.nextLine();
                serveurs.add(ipServeur);

                try {
                    passerelles.add((Inet4Address) Inet4Address.getByName(ipServeur));
                } catch (UnknownHostException e) {
                    System.out.println("IP invalide, passerelle ignorée.");
                    passerelles.add(null);
                }

                ArrayList<String> clientsServeur = new ArrayList<>();
                System.out.print("Combien de clients pour ce serveur ? ");
                int nbClients = Integer.parseInt(scanner.nextLine());
                for (int j = 0; j < nbClients; j++) {
                    System.out.print("Nom du client #" + (j + 1) + " : ");
                    String nomClient = scanner.nextLine();
                    clientsServeur.add(nomClient);
                }
                clients_serveurs.add(clientsServeur);

                System.out.print("Distance vers ce serveur : ");
                int dist = Integer.parseInt(scanner.nextLine());
                distance.add(dist);
            }

            // Création de la trame de routage
            trame = new Trame_routage(
                    1, // type_message (exemple)
                    null, // serveur_cible
                    ipLocal, // serveur_source
                    serveurs,
                    passerelles,
                    clients_serveurs,
                    distance);

            rootingTable.addTable(trame, (Inet4Address) InetAddress.getByName(ipLocal));

            // Affichage de la table de routage initiale
            System.out.println("Serveurs : " + trame.getServeurs());
            System.out.println("Passerelles : " + trame.getPasserelles());
            System.out.println("Clients par serveur : " + trame.getClients_serveurs());
            System.out.println("Distances : " + trame.getDistance());

            // Préparation à l'envoi de la trame aux autres serveurs
            for (String serveur : serveurs) {
                if (!serveur.equals(ipLocal)) {
                    trame.setServeur_cible(serveur);
                    try {
                        // Ici, tu peux tester la disponibilité du serveur distant si besoin
                        // (code commenté dans la version actuelle)
                    } catch (Exception e) {
                        System.out
                                .println("Erreur lors de la connexion au serveur " + serveur + " : " + e.getMessage());
                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return trame;
    }

    /**
     * Initialise le réseau et lance l'échange des tables de routage.
     * Si le serveur est principal, il envoie la première trame.
     * Attend ensuite les trames de routage des voisins et met à jour la table
     * jusqu'à convergence.
     */
    public void initNetwork() {
        int sameDataCount = 0;

        Trame_routage firstTrame = initTrame();

        if (servPrincipal) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Appuyez sur Entrée pour continuer et envoyer les premières trames de routage...");
            scanner.nextLine();

            // Envoie de la table pour commencer (ici, exemple avec une IP en dur)
            // for (String serveur : rootingTable.getServeurs()) {
            // sendMessage(firstTrame, serveur);
            // System.out.println("Tables de routage envoyées au serveur voisin: " +
            // serveur);
            // }
            sendMessage(firstTrame, "192.168.1.62");
        }

        // Répète tant que les données ne sont pas les mêmes 5 fois de suite
        while (sameDataCount < 5) {
            try {
                System.out.println("En attente ...");
                // Attente d'une trame de routage d'un voisin avec timeout de 5 secondes
                server.setSoTimeout(5000);
                Socket neighborSocket;
                try {
                    neighborSocket = server.accept();
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Aucune demande reçue en 5 secondes, arrêt de l'attente.");
                    System.out.println("========== PASSAGE EN MODE MESSAGE ==========");
                    break;
                } finally {
                    // Remettre le timeout à 0 (infini) si besoin pour les prochaines accept
                    server.setSoTimeout(0);
                }
                System.out.println("Serveur accepté.");

                ObjectInputStream objectIn = new ObjectInputStream(neighborSocket.getInputStream());
                Object receivedObject = objectIn.readObject();

                if (receivedObject instanceof Trame_routage) {
                    Trame_routage trame = (Trame_routage) receivedObject;
                    Inet4Address inet4 = (Inet4Address) InetAddress.getByName(ipLocal);

                    // Si la table de routage a été mise à jour, renvoyer la table aux serveurs
                    // voisins
                    if (rootingTable.addTable(trame, inet4)) {
                        for (String serveur : rootingTable.getServeurs()) {

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            Trame_routage trame_r = rootingTable.exportTable(serveur, this.ipLocal);
                            sendMessage(trame_r, serveur);
                            System.out.println("Tables de routage envoyées au serveur voisin: " + serveur);
                        }
                        sameDataCount = 0;
                    } else {
                        sameDataCount++;
                    }
                } else {
                    sameDataCount = 6;
                }
                neighborSocket.close();
                state = stateServer.RUNNING;
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Erreur lors de la réception de la trame de routage: " + e.getMessage());
            }
        }
    }
}