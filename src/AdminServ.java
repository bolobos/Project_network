// All is starting there, interact with different actors
public class AdminServ {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");


        Thread serverThread = new Thread(() -> {
            new Server().listenSocket();
        });

        Thread clientThread = new Thread(() -> {
            new Client().listenSocket();
        });

        serverThread.start();
        clientThread.start();

        try {
            serverThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
