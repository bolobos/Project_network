import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// ClientHandler is a interface of runnable, it's make the method run() as a function that run when a thread starts
public class ClientHandler implements Runnable {

    // Handle one client that are connect to the server
    private Socket client;

    // Constructor which is assign the client attribute to a specified socket
    public ClientHandler(Socket socket) {
        this.client = socket;
    }

    public void run() {

        try {

            // Objects to communicate with INPUT/OUTPUT
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            //Get data received
            String line = in.readLine();
            System.out.println("SERVER - DATA RECEIVED");

            // Send data back to client
            out.println(line);
            System.out.println("SERVER - DATA SEND");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    
}
