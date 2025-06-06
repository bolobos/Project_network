import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket client;
    private Server server; // Référence au serveur
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket, Server server) {
        this.client = socket;
        this.server = server;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            // Demander le nom du client
            out.println("Enter your name: ");
            clientName = in.readLine();
            System.out.println(clientName + " has joined the chat.");

            // Diffuser un message de bienvenue
            server.broadcastMessage("SERVER: " + clientName + " has joined the chat.", this);

            // Boucle pour traiter les messages
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(clientName + " sent: " + line);

                // Vérifier si le message est une commande pour envoyer à un autre client
                if (line.startsWith("@")) {
                    // Extraire le nom du destinataire et le message
                    int spaceIndex = line.indexOf(' ');
                    if (spaceIndex != -1) {
                        String targetName = line.substring(1, spaceIndex); // Nom du destinataire
                        String message = line.substring(spaceIndex + 1); // Message à envoyer

                        // Trouver le client destinataire
                        ClientHandler targetClient = server.getClientByName(targetName);
                        if (targetClient != null) {
                            // Envoyer le message au destinataire
                            targetClient.sendMessage(clientName + " sent: " + message);

                            // Le serveur espionne et affiche le message
                            System.out.println("SERVER SPY: " + clientName + " sent to " + targetName + ": " + message);
                        } else {
                            out.println("SERVER: Client " + targetName + " not found.");
                        }
                    } else {
                        out.println("SERVER: Invalid command. Use @<clientName> <message>");
                    }
                } else {
                    out.println("SERVER: Invalid command. Use @<clientName> <message>");
                }
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }

            // Supprimer le client de la liste
            server.removeClient(this);
            System.out.println(clientName + " has left the chat.");
            server.broadcastMessage("SERVER: " + clientName + " has left the chat.", this);
        }
    }

    // Méthode pour envoyer un message à ce client
    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }
}
