import java.util.Scanner;

// Interface pour créer des clients
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
                            while (clientName.isEmpty()) {
                                System.out.print("Client name cannot be empty. Enter client name: ");
                                clientName = scanner.nextLine().trim();
                            }

                            System.out.print("Enter client ID: ");
                            String clientId = scanner.nextLine().trim();
                            while (clientId.isEmpty()) {
                                System.out.print("Client ID cannot be empty. Enter client ID: ");
                                clientId = scanner.nextLine().trim();
                            }

                            // Créer et afficher le client
                            Client newClient = new Client(clientName, clientId);
                            System.out.println("Client created: " + newClient);

                            // Lancer un nouveau terminal pour exécuter le client
                            launchClientTerminal(clientName, clientId);
                            break;

                        case 2:
                            // Quitter le programme
                            running = false;
                            System.out.println("Exiting Admin Client...");
                            break;

                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // Consomme l'entrée invalide
                }
            }
        }
    }

    // Méthode pour lancer un nouveau terminal et exécuter le client
    private static void launchClientTerminal(String clientName, String clientId) {
        try {
            // Commande pour ouvrir un terminal et exécuter le client
            String[] command = {
                "gnome-terminal",
                "--",
                "bash",
                "-c",
                String.format("java -cp ../bin Client %s %s; exec bash", clientName, clientId)
            };

            // Utilisation de ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.start();

            System.out.println("Client terminal launched for " + clientName + " (ID: " + clientId + ").");
        } catch (Exception e) {
            System.out.println("Failed to launch client terminal: " + e.getMessage());
        }
    }

    // Classe interne pour représenter un client
    static class Client {
        private String name;
        private String id;

        public Client(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return "Client{name='" + name + "', id='" + id + "'}";
        }
    }
}