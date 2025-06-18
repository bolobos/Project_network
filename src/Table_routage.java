import java.net.Inet4Address;
import java.util.ArrayList;

/**
 * La classe {@code Table_routage} représente une table de routage pour un réseau.
 * Elle maintient les informations sur les serveurs connus, les passerelles associées,
 * les clients reliés à chaque serveur, ainsi que la distance (nombre de sauts) pour atteindre chaque serveur.
 *
 * <p>
 * Les principales fonctionnalités incluent :
 * <ul>
 *   <li>Ajout et mise à jour des entrées de la table de routage à partir d'une trame de routage reçue.</li>
 *   <li>Exportation de la table de routage sous forme de trame pour la transmission à d'autres nœuds.</li>
 *   <li>Accès et modification des listes internes représentant les serveurs, passerelles, clients et distances.</li>
 * </ul>
 * </p>
 *
 * @author bolobos
 */
public class Table_routage {

    /** Liste des adresses IP des serveurs connus. */
    private ArrayList<String> serveurs;

    /** Liste des adresses IP des passerelles associées à chaque serveur. */
    private ArrayList<Inet4Address> passerelles;

    /** Liste des clients associés à chaque serveur. */
    private ArrayList<ArrayList<String>> clients_serveurs;

    /** Liste des distances (nombre de sauts) pour atteindre chaque serveur. */
    private ArrayList<Integer> distance;

    /**
     * Constructeur par défaut. Initialise les listes internes.
     */
    public Table_routage() {
        serveurs = new ArrayList<String>();
        passerelles = new ArrayList<Inet4Address>();
        clients_serveurs = new ArrayList<ArrayList<String>>();
        distance = new ArrayList<Integer>();
    }

    /**
     * Ajoute ou met à jour la table de routage à partir d'une trame reçue.
     * Si un nouveau serveur est découvert, il est ajouté avec sa passerelle, ses clients et sa distance.
     * Si un chemin plus court vers un serveur existant est trouvé, la passerelle et la distance sont mises à jour.
     *
     * @param trame La trame de routage reçue.
     * @param adresseSource L'adresse source de la trame (passerelle).
     * @return {@code true} si la table a été modifiée, {@code false} sinon.
     */
    public boolean addTable(Trame_routage trame, Inet4Address adresseSource) {

        // Serveurs et clients à rajouter
        ArrayList<String> tempServeurs = trame.getServeurs();
        ArrayList<ArrayList<String>> tempClientsServeurs = trame.getClients_serveurs();
        ArrayList<Integer> tempDistance = trame.getDistance();
        boolean res = false;
        LocalIP localIP;

        System.out.println("----- DEBUT TRAITEMENT DE LA TRAME DE ROUTAGE -----");

        // Navigation dans les différents serveurs à rajouter
        for (String serveur : tempServeurs) {

            int index = tempServeurs.indexOf(serveur);

            if (!serveurs.contains(serveur)) {

                res = true;
                serveurs.add(serveur);

                // passerelle = adresse serveur qui envoie la trame
                passerelles.add(adresseSource);
                clients_serveurs.add(tempClientsServeurs.get(index));

                // Pour la distance, incrémentation
                distance.add(tempDistance.get(index) + 1);

                System.out.println("NOUVELLE IP : " + serveur);

            } else {
                
                // Si l'on trouve un chemin pluscourt
                if ((tempDistance.get(index) + 1) < distance.get(index)) {

                    res=true;
                    // Changement de la passerelle
                    passerelles.set(index, adresseSource);
                    distance.set(index, (tempDistance.get(index) + 1));

                    System.out.println(
                            "NOUVEAU CHEMIN POUR IP " + serveur + " DE " + distance.get(index) + " DE DISTANCE.");
                }

            }

            // Si l'adresse IP du serveur est égale à l'adresse IP locale, mettre la distance à 0
            try {
                localIP = new LocalIP();
                String myIP = localIP.getLocalIP();
                if (serveur.equals(myIP)) {
                    distance.set(serveurs.indexOf(serveur), 0);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération de l'adresse IP locale : " + e.getMessage());
            }
        }

        System.out.println("----- FIN TRAITEMENT DE LA TRAME DE ROUTAGE -----");

        
        // Affichage formaté de la table de routage
        System.out.println("===== TABLE DE ROUTAGE =====");
        System.out.printf("%-20s %-20s %-30s %-10s%n", "Serveur", "Passerelle", "Clients Serveurs", "Distance");
        System.out.println("------------------------------------------------------------------------------------------");
        for (int i = 0; i < serveurs.size(); i++) {
            String serveur = serveurs.get(i);
            String passerelle = passerelles.get(i) != null ? passerelles.get(i).getHostAddress() : "N/A";
            String clients = clients_serveurs.get(i) != null ? clients_serveurs.get(i).toString() : "[]";
            int dist = distance.get(i);
            System.out.printf("%-20s %-20s %-30s %-10d%n", serveur, passerelle, clients, dist);
        }
        System.out.println("===== FIN TABLE DE ROUTAGE =====");

        return res;
    }

    /**
     * Exporte la table de routage sous forme de trame pour l'envoyer à un autre serveur.
     *
     * @param serveur_cible L'adresse IP du serveur destinataire.
     * @param serveur_source L'adresse IP du serveur source.
     * @return Une nouvelle instance de {@code Trame_routage} contenant la table actuelle.
     */
    public Trame_routage exportTable(String serveur_cible, String serveur_source) {
        Trame_routage trame = new Trame_routage(1, serveur_cible, serveur_source, serveurs, passerelles,
                clients_serveurs, distance);
        return trame;
    }

    /**
     * Retourne la liste des serveurs connus.
     * @return Liste des adresses IP des serveurs.
     */
    public ArrayList<String> getServeurs() {
        return serveurs;
    }

    /**
     * Définit la liste des serveurs connus.
     * @param serveurs Liste des adresses IP des serveurs.
     */
    public void setServeurs(ArrayList<String> serveurs) {
        this.serveurs = serveurs;
    }

    /**
     * Retourne la liste des passerelles associées à chaque serveur.
     * @return Liste des adresses IP des passerelles.
     */
    public ArrayList<Inet4Address> getPasserelles() {
        return passerelles;
    }

    /**
     * Définit la liste des passerelles associées à chaque serveur.
     * @param passerelles Liste des adresses IP des passerelles.
     */
    public void setPasserelles(ArrayList<Inet4Address> passerelles) {
        this.passerelles = passerelles;
    }

    /**
     * Retourne la liste des clients associés à chaque serveur.
     * @return Liste des listes de clients par serveur.
     */
    public ArrayList<ArrayList<String>> getClients_serveurs() {
        return clients_serveurs;
    }

    /**
     * Définit la liste des clients associés à chaque serveur.
     * @param clients_serveurs Liste des listes de clients par serveur.
     */
    public void setClients_serveurs(ArrayList<ArrayList<String>> clients_serveurs) {
        this.clients_serveurs = clients_serveurs;
    }

    /**
     * Retourne la liste des distances pour atteindre chaque serveur.
     * @return Liste des distances (nombre de sauts).
     */
    public ArrayList<Integer> getDistance() {
        return distance;
    }

    /**
     * Définit la liste des distances pour atteindre chaque serveur.
     * @param distance Liste des distances (nombre de sauts).
     */
    public void setDistance(ArrayList<Integer> distance) {
        this.distance = distance;
    }

}