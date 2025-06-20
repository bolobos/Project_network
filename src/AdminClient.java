import java.util.Scanner;

// Classe principale pour l'administration des clients
public class AdminClient {
    public static void main(String[] args) {
        // Utilisation de try-with-resources pour garantir la fermeture du Scanner
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            // Boucle principale pour gérer le menu
            while (running) {
                System.out.println("Admin Client Menu:");
                System.out.println("1. Create a new client");
                System.out.println("2. Exit");
                System.out.print("Enter your choice: ");

                // Vérification de l'entrée utilisateur
                if (scanner.hasNextInt()) {
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consomme le retour à la ligne

                    switch (choice) {
                        case 1:
                            // Demander les détails du client
                            System.out.print("Enter client name: ");
                            String clientName = scanner.nextLine().trim();
                            // Vérifie que le nom du client n'est pas vide
                            while (clientName.isEmpty()) {
                                System.out.print("Client name cannot be empty. Enter client name: ");
                                clientName = scanner.nextLine().trim();
                            }

                            System.out.print("Enter client ID: ");
                            String clientId = scanner.nextLine().trim();
                            // Vérifie que l'ID du client n'est pas vide
                            while (clientId.isEmpty()) {
                                System.out.print("Client ID cannot be empty. Enter client ID: ");
                                clientId = scanner.nextLine().trim();
                            }

                            System.out.print("Enter server IP: ");
                            String ipServ = scanner.nextLine().trim();
                            // Vérifie que l'IP du serveur n'est pas vide
                            while (ipServ.isEmpty()) {
                                System.out.print("Server IP cannot be empty. Enter server IP: ");
                                ipServ = scanner.nextLine().trim();
                            }

                            // Créer et afficher le client
                            Client newClient = new Client(clientName, clientId, ipServ);
                            System.out.println("Client created: " + newClient);

                            // Lancer un nouveau terminal pour exécuter le client
                            launchClientTerminal(clientName, clientId, ipServ);
                            break;

                        case 2:
                            // Quitter le programme
                            running = false;
                            System.out.println("Exiting Admin Client...");
                            break;

                        default:
                            // Gestion d'un choix invalide
                            System.out.println("Invalid choice. Please try again.");
                    }
                } else {
                    // Gestion d'une entrée non numérique
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // Consomme l'entrée invalide
                }
            }
        }
    }

    /**
     * Méthode pour lancer un nouveau terminal et exécuter le client avec les paramètres donnés.
     * @param clientName Nom du client
     * @param clientId ID du client
     * @param ipServ Adresse IP du serveur
     */
    private static void launchClientTerminal(String clientName, String clientId, String ipServ) {
        try {
            // Commande pour ouvrir un terminal GNOME et exécuter la classe Client
            String[] command = {
                "gnome-terminal",
                "--",
                "bash",
                "-c",
                String.format("java -cp ../bin Client %s %s %s; exec bash", clientName, clientId, ipServ)
            };

            // Utilisation de ProcessBuilder pour lancer la commande
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.start();

            System.out.println("Client terminal launched for " + clientName + " (ID: " + clientId + ").");
        } catch (Exception e) {
            // Affiche un message d'erreur si le lancement échoue
            System.out.println("Failed to launch client terminal: " + e.getMessage());
        }
    }

    // Classe interne pour représenter un client
    static class Client {
        private String name;
        private String id;
        private String ip;

        /**
         * Constructeur de la classe Client.
         * @param name Nom du client
         * @param id ID du client
         * @param ipServ Adresse IP du serveur
         */
        public Client(String name, String id, String ipServ) {
            this.name = name;
            this.id = id;
            this.ip = ipServ;
        }

        @Override
        public String toString() {
            return "Client{name='" + name + "', id='" + id + "'}";
        }
    }
}