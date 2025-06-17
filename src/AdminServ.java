// Classe principale pour démarrer le serveur d'administration
public class AdminServ {
    public static void main(String[] args) throws Exception {
        // Affiche un message de démarrage
        System.out.println("Démarrage du serveur...");

        // Crée un thread pour exécuter le serveur
        Thread serverThread = new Thread(() -> {
            new Server().listenSocket(); // Lance l'écoute du serveur
        });

        // Exemple de création d'un thread client (commenté)
        // Thread clientThread = new Thread(() -> {
        //     // Fournir les arguments requis pour le client
        //     new Client().listenSocket("Alice", "123"); // Exemple de clientName et clientId
        // });

        // Démarre le thread serveur
        serverThread.start();
        // Démarre le thread client (commenté)
        //clientThread.start();

        try {
            // Attend la fin du thread serveur
            serverThread.join();
            // Attend la fin du thread client (commenté)
            //clientThread.join();
        } catch (InterruptedException e) {
            // Affiche la trace de l'exception en cas d'interruption
            e.printStackTrace();
        }
    }
}
