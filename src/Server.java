
// Library for exception
import java.io.IOException;

// Handle sockets
import java.net.ServerSocket;
import java.net.Socket;

// Server class handle the server, it using TCP communication via socket objects
public class Server {

    // Object server // Main component who represent the server as a socket
    public ServerSocket server = null;

    private int port_server = 9081;

    public void listenSocket() {

        try {
            // Create the server
            ServerSocket server = new ServerSocket(port_server);
            PublicIP publicIP = new PublicIP();
            System.out.println("Server create at " + port_server + " port with the IP : " + publicIP.getPublicIP());

            // Actions when stopping the server
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                try {
                    if (server != null && !server.isClosed()) {
                        server.close(); // close the listening socket
                        System.out.println("Server socket closed.");

                        //TODO potentioellement fermer tous les objets clients à la fin
                    }
                } catch (IOException e) {
                    System.out.println("Error closing server socket: " + e.getMessage());
                }
            }));

            // Loop for new clients
            while (true) {
                // Wait for a client / blocking function
                Socket client = server.accept();
                System.out.println("Client connected.");

                // TODO : add table de routage

                // Handle client in a new thread
                ClientHandler handler = new ClientHandler(client);
                new Thread(handler).start();

            }

        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
            System.exit(-1);
        }

    }

    public ServerSocket getServer() {
        return server;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }

    public int getPort_server() {
        return port_server;
    }

    public void setPort_server(int port_server) {
        this.port_server = port_server;
    }

}
