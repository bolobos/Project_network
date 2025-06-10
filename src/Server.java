import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

// Server class handle the server, it using TCP communication via socket objects
public class Server {

    // Object server // Main component who represent the server as a socket
    private ServerSocket server = null;

    public enum stateServer {
        INIT,
        RUNNING,
        STOPPED
    }

    private stateServer state = stateServer.INIT;

    private int port_server = 9081;

    private List<ClientHandler> clients = new ArrayList<>(); // Liste des clients connectés

    public void listenSocket() {

        try {
            // Créer le serveur
            server = new ServerSocket(port_server);
            PublicIP publicIP = new PublicIP();
            System.out.println("Server created at port " + port_server + " with IP: " + publicIP.getPublicIP());

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

            /*while (true) {
                FileOutputStream fileOutputStream = new FileOutputStream("yourfile.txt");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(person);
                objectOutputStream.flush();
                objectOutputStream.close();

                FileInputStream fileInputStream = new FileInputStream("yourfile.txt");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                Person p2 = (Person) objectInputStream.readObject();
                objectInputStream.close();

                assertTrue(p2.getAge() == person.getAge());
                assertTrue(p2.getName().equals(person.getName()));
            }*/

            // Boucle pour accepter les nouveaux clients
            while (true) {
                // Wait for a client / blocking function
                Socket client = server.accept();
                System.out.println("Client connected.");

                // Créer un ClientHandler pour ce client
                ClientHandler handler = new ClientHandler(client, this);
                clients.add(handler); // Ajouter le client à la liste
                new Thread(handler).start();
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

    // Méthode pour diffuser un message à tous les clients
    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // Ne pas renvoyer le message à l'expéditeur
                client.sendMessage(message);
            }
        }
    }

    // Méthode pour supprimer un client de la liste lorsqu'il se déconnecte
    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public synchronized ClientHandler getClientByName(String name) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null; // Retourne null si aucun client avec ce nom n'est trouvé
    }
}
