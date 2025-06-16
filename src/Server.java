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
import java.util.Scanner;

// Server class handle the server, it uses TCP communication via socket objects
public class Server {

    private ServerSocket server = null;
    private int port_server = 9081;
    private String ipLocal;
    private boolean servPrincipal=false;

    private Table_routage rootingTable = new Table_routage();

    public enum stateServer {
        INIT,
        RUNNING,
        STOPPED
    }

    private stateServer state = stateServer.INIT;

    public void listenSocket() {
        while (true) {
            switch (state) {
                case INIT:
                    try {

                        // INIT OF SERVER

                        // Créer le serveur
                        server = new ServerSocket(port_server);
                        LocalIP localIP = new LocalIP();
                        ipLocal = localIP.getLocalIP();
                        System.out
                                .println("Server created at port " + port_server + " with IP: " + this.ipLocal);

                        // Actions lors de l'arrêt du serveur
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            System.out.println("Shutting down server...");
                            state = stateServer.STOPPED;
                        }));

                        // INIT PART FOR ROOTING

                        // Initialiser le réseau
                        initNetwork();

                        // End of init -> start the RUNNING PHASE
                        state = stateServer.RUNNING;

                    } catch (IOException e) {
                        System.out.println("Erreur : " + e.getMessage());
                        state = stateServer.STOPPED;
                    }
                    break;
                case RUNNING:
                    try {

                        // Boucle pour accepter les nouveaux clients ou serveurs
                        System.out.println("En attente de réception...");
                        Socket client = server.accept();
                        System.out.println(
                                "Client or server connected : " + client.getInetAddress().getHostAddress());

                        try {
                            ObjectInputStream objectIn = new ObjectInputStream(client.getInputStream());
                            Object receivedObject = objectIn.readObject();

                            if (receivedObject instanceof Trame_message) {
                                Trame_message receivedMessage = (Trame_message) receivedObject;

                                // Affichage commun
                                System.out.println("Message reçu : " + receivedMessage.getDu());
                                System.out.println("Depuis : " + receivedMessage.getServeur_source());
                                System.out.println("Pour le client : " + receivedMessage.getClient_cible());

                                int res = receivedMessage.redirectMessage(receivedMessage, rootingTable, ipLocal);

                                switch (res) {
                                    case 1:
                                        // Message destiné au serveur courant
                                        System.out.println("Message destiné au serveur courant.");
                                        // Traiter le message ici si besoin
                                        break;
                                    case 2:
                                        // Message destiné à un client local
                                        System.out.println("Message destiné à un client local.");
                                        handleTrameMessage(receivedMessage, res);
                                        break;
                                    case 3:
                                        // Message destiné à un autre serveur (client cible non local)
                                        System.out.println(
                                                "Message destiné à un autre serveur (client cible non local).");
                                        handleTrameMessage(receivedMessage, res);
                                        break;
                                    case 4:
                                        // Message destiné à un autre serveur (serveur cible différent)
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

    public void handleTrameMessage(Trame_message trame, int res) {
        String client_cible = trame.getClient_cible();

        switch (res) {
            case 2:
                // Message destiné à un client local
                for (int i = 0; i < rootingTable.getClients_serveurs().size(); i++) {
                    ArrayList<String> clients = rootingTable.getClients_serveurs().get(i);
                    if (clients.contains(client_cible)) {
                        sendMessage(trame, client_cible);
                        System.out.println("Message envoyé au client local : " + client_cible);
                        return;
                    }
                }
                System.out.println("Client local introuvable : " + client_cible);
                break;

            case 3:
            case 4:
                // Message destiné à un autre serveur (client cible non local ou serveur cible
                // différent)
                for (int i = 0; i < rootingTable.getClients_serveurs().size(); i++) {
                    ArrayList<String> clients = rootingTable.getClients_serveurs().get(i);
                    if (clients.contains(client_cible)) {
                        String passerelle = rootingTable.getPasserelles().get(i).getHostAddress();
                        sendMessage(trame, passerelle);
                        System.out.println(
                                "Message relayé à la passerelle : " + passerelle + " pour client " + client_cible);
                        return;
                    }
                }
                System.out.println("Passerelle introuvable pour client : " + client_cible);
                break;

            default:
                System.out.println("Aucune action pour ce code de redirection : " + res);
                break;
        }
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

    public Trame_routage initTrame() {

        Trame_routage trame = null;

        // Exemple de création d'une trame de routage
        try {
            // Création d'un menu pour entrer les IP des serveurs et les clients associés
            ArrayList<String> serveurs = new ArrayList<>();
            ArrayList<Inet4Address> passerelles = new ArrayList<>();
            ArrayList<ArrayList<String>> clients_serveurs = new ArrayList<>();
            ArrayList<Integer> distance = new ArrayList<>();

            Scanner scanner = new Scanner(System.in);

            System.out.print("Ce serveur est-il le serveur principal ? (oui/non) : ");
            String principalInput = scanner.nextLine().trim().toLowerCase();
            if (principalInput.equals("oui") || principalInput.equals("o") || principalInput.equals("yes")) {
                servPrincipal = true;
            } else {
                servPrincipal = false;
            }

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

            // Utilisation de la trame
            System.out.println("Serveurs : " + trame.getServeurs());
            System.out.println("Passerelles : " + trame.getPasserelles());
            System.out.println("Clients par serveur : " + trame.getClients_serveurs());
            System.out.println("Distances : " + trame.getDistance());

            for (String serveur : serveurs) {

                if (!serveur.equals(ipLocal)) {
                    trame.setServeur_cible(serveur);
                    try {
                        // Attendre que le serveur distant soit prêt à accepter la connexion
                        boolean connected = false;
                        while (!connected) {
                            try {
                                SocketAddress sockaddr = new InetSocketAddress(serveur, 9081);
                                try (Socket testSocket = new Socket()) {
                                    testSocket.connect(sockaddr, 1000); // timeout de 1000 ms
                                    System.out.println("Connexion réussie à " + serveur);
                                    connected = true;
                                }
                            } catch (IOException e) {
                                System.out.println(
                                        "Échec de connexion à " + serveur + ", nouvelle tentative dans 500ms...");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out
                                .println("Erreur lors de la connexion au serveur " + serveur + " : " + e.getMessage());
                    }
                    sendMessage(trame, serveur);
                }

            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return trame;

    }

    public void initNetwork() {

        int sameDataCount = 0;

        // Répète tant que les données ne sont pas les mêmes 5 fois de suite
        while (sameDataCount < 5) {

            // Attendre la première trame de routage d'un voisin
            try {

                Trame_routage firstTrame = initTrame();

                if (servPrincipal) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Appuyez sur Entrée pour continuer et envoyer les premières trames de routage...");
                    scanner.nextLine();

                    // Envoie de la table pour commencer
                    for (String serveur : rootingTable.getServeurs()) {
                        sendMessage(firstTrame, serveur);
                        System.out.println("Tables de routage envoyées au serveur voisin: " + serveur);
                    }
                }

                // Fonction bloquante
                Socket neighborSocket = server.accept();

                ObjectInputStream objectIn = new ObjectInputStream(neighborSocket.getInputStream());
                Object receivedObject = objectIn.readObject();

                if (receivedObject instanceof Trame_routage) {
                    Trame_routage trame = (Trame_routage) receivedObject;
                    Inet4Address inet4 = (Inet4Address) InetAddress.getByName(ipLocal);

                    // Si la table de routage a été mise à jour, renvoyer la table aux serveurs
                    // voisins
                    if (rootingTable.addTable(trame, inet4)) {
                        for (String serveur : rootingTable.getServeurs()) {
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