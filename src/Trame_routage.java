import java.net.Inet4Address;
import java.util.ArrayList;

/**
 * Représente une trame de routage échangée entre serveurs dans le réseau.
 * Cette trame transporte l'état de la table de routage d'un serveur :
 * la liste des serveurs connus, leurs passerelles, les clients associés et la distance pour chaque serveur.
 */
public class Trame_routage extends Trame {

    /** Liste des adresses IP des serveurs connus. */
    private ArrayList<String> serveurs;

    /** Liste des adresses IP des passerelles associées à chaque serveur. */
    private ArrayList<Inet4Address> passerelles;

    /** Liste des clients associés à chaque serveur. */
    private ArrayList<ArrayList<String>> clients_serveurs;

    /** Liste des distances (nombre de sauts) pour atteindre chaque serveur. */
    private ArrayList<Integer> distance;

    /**
     * Construit une nouvelle trame de routage.
     *
     * @param type_message    Type du message (entier).
     * @param serveur_cible   Nom ou IP du serveur destinataire.
     * @param serveur_source  Nom ou IP du serveur source.
     * @param serveurs        Liste des serveurs connus.
     * @param passerelles     Liste des passerelles associées à chaque serveur.
     * @param clients_serveurs Liste des clients associés à chaque serveur.
     * @param distance        Liste des distances pour atteindre chaque serveur.
     */
    public Trame_routage(int type_message, String serveur_cible,
            String serveur_source, ArrayList<String> serveurs, ArrayList<Inet4Address> passerelles,
            ArrayList<ArrayList<String>> clients_serveurs, ArrayList<Integer> distance) {

        super(type_message, serveur_cible, serveur_source);
        this.setServeurs(serveurs);
        this.setPasserelles(passerelles);
        this.setClients_serveurs(clients_serveurs);
        this.setDistance(distance);
    }

    /** @return La liste des serveurs connus. */
    public ArrayList<String> getServeurs() {
        return serveurs;
    }

    /** Définit la liste des serveurs connus. */
    public void setServeurs(ArrayList<String> serveurs) {
        this.serveurs = serveurs;
    }

    /** @return La liste des passerelles associées à chaque serveur. */
    public ArrayList<Inet4Address> getPasserelles() {
        return passerelles;
    }

    /** Définit la liste des passerelles associées à chaque serveur. */
    public void setPasserelles(ArrayList<Inet4Address> passerelles) {
        this.passerelles = passerelles;
    }

    /** @return La liste des clients associés à chaque serveur. */
    public ArrayList<ArrayList<String>> getClients_serveurs() {
        return clients_serveurs;
    }

    /** Définit la liste des clients associés à chaque serveur. */
    public void setClients_serveurs(ArrayList<ArrayList<String>> clients_serveurs) {
        this.clients_serveurs = clients_serveurs;
    }

    /** Définit la liste des distances pour atteindre chaque serveur. */
    public void setDistance(ArrayList<Integer> distance) {
        this.distance = distance;
    }

    /** @return La liste des distances (nombre de sauts) pour atteindre chaque serveur. */
    public ArrayList<Integer> getDistance() {
        return distance;
    }
}
