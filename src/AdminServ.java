// 
public class AdminServ {
    public static void main(String[] args) throws Exception {
        System.out.println("Démarrage du serveur...");

        Thread serverThread = new Thread(() -> {
            new Server().listenSocket();
        });

        // Thread clientThread = new Thread(() -> {
        //     // Fournir les arguments requis pour le client
        //     new Client().listenSocket("Alice", "123"); // Exemple de clientName et clientId
        // });

        serverThread.start();
        //clientThread.start();

        try {
            serverThread.join();
            //clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
