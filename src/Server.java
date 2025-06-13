import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// Server class handle the server, it uses TCP communication via socket objects
public class Server {

    private ServerSocket server = null;
    private int port_server = 9081;
    private ArrayList<Client> clients = new ArrayList<>(); // Liste des clients connectés
    private ArrayList<String> neighborServers = new ArrayList<>(); // Liste des serveurs voisins

    ArrayList<Trame_routage> routingTables = new ArrayList<>(); // Table de routage

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
                        System.out
                                .println("Server created at port " + port_server + " with IP: " + localIP.getLocalIP());

                        // Actions lors de l'arrêt du serveur
                        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        // System.out.println("Shutting down server...");
                        // try {
                        // if (server != null && !server.isClosed()) {
                        // server.close(); // Fermer le socket d'écoute
                        // System.out.println("Server socket closed.");
                        // }
                        // } catch (IOException e) {
                        // System.out.println("Error closing server socket: " + e.getMessage());
                        // }
                        // }));

                        // INIT PART FOR ROOTING

                        // Initialiser le réseau
                        // initNetwork();

                        // Client client = new Client();

                        // ServerDef serverDef = new ServerDef()

                        // routingTables.add(new Trame_routage())

                        // End of init -> start the RUNNING PHASE
                        state = stateServer.RUNNING;

                    } catch (IOException e) {
                        System.out.println("Erreur : " + e.getMessage());
                        state = stateServer.STOPPED;
                    }
                    break;
                case RUNNING:
                    try {
                        // while (true) {
                        // // Wait for a client / blocking function
                        // Socket client = server.accept();
                        // System.out.println("Client connected.");

                        // // TODO : add table de routage

                        // // Handle client in a new thread
                        // ClientHandler handler = new ClientHandler(client);
                        // new Thread(handler).start();
                        // }

                        // Boucle pour accepter les nouveaux clients ou serveurs
                        while (state == stateServer.RUNNING) {
                            Socket client = server.accept();
                            System.out.println(
                                    "Client or server connected : " + client.getInetAddress().getHostAddress());
                            try {
                                ObjectInputStream objectIn = new ObjectInputStream(client.getInputStream());
                                Object receivedObject = objectIn.readObject();

                                if (receivedObject instanceof Trame_message) {
                                    Trame_message receivedMessage = (Trame_message) receivedObject;

                                    System.out.println(
                                            "Message reçu de l'adresse IP " + receivedMessage.getClient_source()
                                                    + " : " + receivedMessage.getDu());

                                }

                                client.close();
                            } catch (IOException | ClassNotFoundException e) {
                                System.out.println("Error handling connection: " + e.getMessage());
                            }
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

    public void initNetwork() {
        // Fill the servers and clients that we know
        clients.add(new Client("newClient1", "1"));
        clients.add(new Client("newClient2", "2"));

        neighborServers.add("XXX.XXX.XXX.XXX");
        neighborServers.add("XXX.XXX.XXX.XXX");

        int sameDataCount = 0;

        while (sameDataCount < 5) {
            // Attendre la première trame de routage d'un voisin
            try {
                // Fonction bloaquante
                Socket neighborSocket = server.accept();
                ObjectInputStream objectIn = new ObjectInputStream(neighborSocket.getInputStream());
                Object receivedObject = objectIn.readObject();

                if (receivedObject instanceof Trame_routage) {
                    Trame_routage receivedTable = (Trame_routage) receivedObject;
                    boolean updated = false;

                    // Vérifier si la trame reçue contient de nouvelles données
                    for (Trame_routage.ServerDef serverDef : receivedTable.listServerDefs) {
                        boolean exists = false;

                        // Parcourir toutes les tables de routage stockées
                        for (Trame_routage trame : routingTables) {
                            for (Trame_routage.ServerDef def : trame.listServerDefs) {
                                if (def.adressServer.equals(serverDef.adressServer)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists)
                                break;
                        }
                        if (!exists) {
                            // Ajouter le serverDef dans une nouvelle trame_routage ou dans une existante
                            // selon votre logique
                            Trame_routage newTrame = new Trame_routage();
                            newTrame.listServerDefs.add(serverDef);
                            routingTables.add(newTrame);
                            updated = true;
                            System.out.println(
                                    "Nouveau serveur ajouté à la table de routage: "
                                            + serverDef.adressServer);
                        }
                    }

                    // Si la table de routage a été mise à jour, renvoyer la table aux serveurs
                    // voisins
                    if (updated) {
                        for (String neighbor : neighborServers) {
                            try {
                                String ip = neighbor;
                                int port = 9081;
                                Socket socket = new Socket(ip, port);
                                ObjectOutputStream objectOut = new ObjectOutputStream(
                                        socket.getOutputStream());
                                // Envoyer chaque trame_routage de la table de routage
                                for (Trame_routage trame : routingTables) {
                                    objectOut.writeObject(trame);
                                    objectOut.flush();
                                }
                                socket.close();
                                System.out.println(
                                        "Tables de routage envoyées au serveur voisin: " + neighbor);
                            } catch (IOException e) {
                                System.out
                                        .println("Erreur lors de l'envoi de la table à " + neighbor + " : "
                                                + e.getMessage());
                            }
                        }
                        sameDataCount = 0;

                    } else {
                        sameDataCount++;
                    }

                }
                neighborSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Erreur lors de la réception de la trame de routage: " + e.getMessage());
            }
        }
    }

    // public synchronized void updateRoutingTable(Trame_routage receivedTable) {
    // System.out.println("Updating routing table...");
    // for (Trame_routage.ServerDef serverDef : receivedTable.listServerDefs) {
    // boolean exists = routingTable.listServerDefs.stream()
    // .anyMatch(def -> def.adressServer.equals(serverDef.adressServer));
    // if (!exists) {
    // routingTable.listServerDefs.add(serverDef);
    // System.out.println("Added new server to routing table: " +
    // serverDef.adressServer);
    // }
    // }
    // System.out.println("Routing table updated.");
    // }

    // // Méthode pour diffuser un message à tous les clients
    // public synchronized void broadcastMessage(String message, ClientHandler
    // sender) {
    // System.out.println("Broadcasting message: " + message);
    // for (ClientHandler client : clients) {
    // if (client != sender) { // Ne pas renvoyer le message à l'expéditeur
    // client.sendMessage(message);
    // System.out.println("Message sent to client: " + client.getClientName());
    // }
    // }
    // }

    // // Méthode pour supprimer un client de la liste lorsqu'il se déconnecte
    // public synchronized void removeClient(ClientHandler client) {
    // clients.remove(client);
    // System.out.println("Removed client: " + client.getClientName());
    // }

    // public synchronized ClientHandler getClientByName(String name) {
    // for (ClientHandler client : clients) {
    // if (client.getClientName().equalsIgnoreCase(name)) {
    // System.out.println("Found client by name: " + name);
    // return client;
    // }
    // }
    // System.out.println("Client not found by name: " + name);
    // return null; // Retourne null si aucun client avec ce nom n'est trouvé
    // }
}