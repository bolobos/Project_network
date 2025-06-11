import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Server class handle the server, it uses TCP communication via socket objects
public class Server {

    private ServerSocket server = null;
    private int port_server = 9081;
    private List<ClientHandler> clients = new ArrayList<>(); // Liste des clients connectés
    private List<String> neighborServers = new ArrayList<>(); // Liste des serveurs voisins
    private Trame_routage routingTable = new Trame_routage(); // Table de routage

    public enum stateServer {
        INIT,
        RUNNING,
        STOPPED
    }

    private stateServer state = stateServer.INIT;

    public void listenSocket() {
        try {
            // Créer le serveur
            server = new ServerSocket(port_server);
            PublicIP publicIP = new PublicIP();
            System.out.println("Server created at port " + port_server + " with IP: " + publicIP.getPublicIP());

            // Initialiser le réseau
            initNetwork();

            // Actions lors de l'arrêt du serveur
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                try {
                    if (server != null && !server.isClosed()) {
                        server.close(); // Fermer le socket d'écoute
                        System.out.println("Server socket closed.");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing server socket: " + e.getMessage());
                }
            }));

            // Boucle pour accepter les nouveaux clients ou serveurs
            while (true) {
                Socket client = server.accept();
                System.out.println("Client or server connected.");

                try {
                    ObjectInputStream objectIn = new ObjectInputStream(client.getInputStream());
                    Object receivedObject = objectIn.readObject();

                    if (receivedObject instanceof Trame_routage) {
                        System.out.println("Received routing table from neighbor server:");
                        System.out.println(receivedObject);
                        // Mettre à jour la table de routage
                        updateRoutingTable((Trame_routage) receivedObject);
                    } else {
                        System.out.println("Unknown object received: " + receivedObject.getClass().getName());
                    }

                    client.close();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error handling connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
            if (server != null && !server.isClosed()) {
                try {
                    server.close();
                } catch (IOException ex) {
                    System.out.println("Erreur lors de la fermeture du serveur : " + ex.getMessage());
                }
            }
        }
    }

    public void initNetwork() {
        System.out.println("Initializing network discovery...");

        // Ajouter les serveurs voisins connus au démarrage
        neighborServers.add("192.168.1.2:9081"); // Exemple d'adresse IP et port
        neighborServers.add("192.168.1.3:9081"); // Exemple d'adresse IP et port

        for (String neighbor : neighborServers) {
            try {
                // Extraire l'IP et le port du serveur voisin
                String[] parts = neighbor.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);

                // Se connecter au serveur voisin
                Socket socket = new Socket(ip, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Envoyer une requête pour découvrir les clients et serveurs connectés
                out.println("DISCOVER");
                System.out.println("Sent DISCOVER request to neighbor server: " + ip + ":" + port);
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Received response from " + neighbor + ": " + response);

                    // Ajouter les clients et serveurs découverts à la table de routage
                    if (response.startsWith("CLIENT:")) {
                        String clientInfo = response.substring(7); // Extraire les infos du client
                        String[] clientParts = clientInfo.split(","); // Supposons que les infos sont séparées par des virgules
                        if (clientParts.length == 2) {
                            String clientName = clientParts[0].trim();
                            String clientId = clientParts[1].trim();

                            Trame_routage.ServerDef serverDef = routingTable.new ServerDef();
                            serverDef.listClientsDef.add(new Client(clientName, clientId)); // Ajouter le client
                            serverDef.adressServer = ip + ":" + port;
                            routingTable.listServerDefs.add(serverDef);
                            System.out.println("Added client to routing table: " + clientName + " (ID: " + clientId + ")");
                        } else {
                            System.out.println("Invalid client data format: " + clientInfo);
                        }
                    } else if (response.startsWith("SERVER:")) {
                        String serverInfo = response.substring(7); // Extraire les infos du serveur
                        if (!neighborServers.contains(serverInfo)) {
                            neighborServers.add(serverInfo); // Ajouter à la liste des serveurs voisins
                            System.out.println("Added neighbor server to list: " + serverInfo);
                        }
                    }
                }

                // Envoyer la table de routage au serveur voisin
                ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
                objectOut.writeObject(routingTable);
                objectOut.flush();
                System.out.println("Sent routing table to neighbor server: " + ip + ":" + port);

                // Fermer la connexion
                socket.close();
            } catch (IOException e) {
                System.out.println("Error connecting to neighbor server " + neighbor + ": " + e.getMessage());
            }
        }

        System.out.println("Network discovery completed.");
        System.out.println("Discovered servers: " + neighborServers.size());
    }

    public synchronized void updateRoutingTable(Trame_routage receivedTable) {
        System.out.println("Updating routing table...");
        for (Trame_routage.ServerDef serverDef : receivedTable.listServerDefs) {
            boolean exists = routingTable.listServerDefs.stream()
                .anyMatch(def -> def.adressServer.equals(serverDef.adressServer));
            if (!exists) {
                routingTable.listServerDefs.add(serverDef);
                System.out.println("Added new server to routing table: " + serverDef.adressServer);
            }
        }
        System.out.println("Routing table updated.");
    }

    // Méthode pour diffuser un message à tous les clients
    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        System.out.println("Broadcasting message: " + message);
        for (ClientHandler client : clients) {
            if (client != sender) { // Ne pas renvoyer le message à l'expéditeur
                client.sendMessage(message);
                System.out.println("Message sent to client: " + client.getClientName());
            }
        }
    }

    // Méthode pour supprimer un client de la liste lorsqu'il se déconnecte
    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Removed client: " + client.getClientName());
    }

    public synchronized ClientHandler getClientByName(String name) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equalsIgnoreCase(name)) {
                System.out.println("Found client by name: " + name);
                return client;
            }
        }
        System.out.println("Client not found by name: " + name);
        return null; // Retourne null si aucun client avec ce nom n'est trouvé
    }
}