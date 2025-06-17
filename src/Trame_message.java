import java.util.ArrayList;

/**
 * Représente une trame de message échangée entre serveurs et clients dans le réseau.
 * Hérite de la classe {@code Trame} et ajoute les informations spécifiques à un message client.
 */
public class Trame_message extends Trame {

    /** Nom ou identifiant du client destinataire du message. */
    private String client_cible;

    /** Nom ou identifiant du client source du message. */
    private String client_source;

    /** Données utiles transportées par la trame (contenu du message). */
    private String du;

    /**
     * Construit une nouvelle trame de message.
     *
     * @param type_message   Type du message (entier).
     * @param serveur_cible  Nom ou IP du serveur destinataire.
     * @param serveur_source Nom ou IP du serveur source.
     * @param client_cible   Nom ou identifiant du client destinataire.
     * @param client_source  Nom ou identifiant du client source.
     * @param du             Données utiles (contenu du message).
     */
    public Trame_message(int type_message, String serveur_cible, String serveur_source, String client_cible,
                         String client_source, String du) {
        super(type_message, serveur_cible, serveur_source);
        this.setClient_cible(client_cible);
        this.setClient_source(client_source);
        this.setDu(du);
    }

    /**
     * Détermine le type de redirection nécessaire pour un message donné en fonction
     * de la cible et de la table de routage.
     *
     * @param trame          Le message à analyser.
     * @param routingTable   La table de routage contenant les associations client-serveur.
     * @param serveurCourant Le nom ou l'IP du serveur courant.
     * @return Un code entier indiquant la destination du message :
     *         <ul>
     *         <li>1 : Message destiné au serveur courant.</li>
     *         <li>2 : Message destiné à un client local (présent dans la table de routage et associé au serveur courant).</li>
     *         <li>3 : Message destiné à un autre serveur (le client cible n'est pas local).</li>
     *         <li>4 : Message destiné à un autre serveur (le serveur cible n'est pas le serveur courant).</li>
     *         </ul>
     */
    public int redirectMessage(Trame_message trame, Table_routage routingTable, String serveurCourant) {
        // Si le client cible est null ou vide, c'est un message au serveur
        if (trame.getClient_cible() == null || trame.getClient_cible().isEmpty()) {
            // Vérifie si le serveur cible est le serveur courant
            if (trame.getServeur_cible() != null && trame.getServeur_cible().equals(serveurCourant)) {
                return 1; // Message destiné au serveur courant
            } else {
                return 4; // Message destiné à un autre serveur
            }
        }

        // Pour chaque liste de clients associée à un serveur
        for (int i = 0; i < routingTable.getClients_serveurs().size(); i++) {
            ArrayList<String> clients = routingTable.getClients_serveurs().get(i);
            String serveurAssocie = routingTable.getServeurs().get(i);

            if (clients.contains(trame.getClient_cible())) {
                if (serveurAssocie.equals(serveurCourant)) {
                    return 2; // Message à un client local
                } else if (serveurAssocie.equals(trame.getServeur_source())) {
                    return 3; // Message à un client distant (via ce serveur)
                }
            }
        }

        // Sinon, c'est un message à un autre serveur
        return 3;
    }

    /** @return Le nom ou identifiant du client cible. */
    public String getClient_cible() {
        return client_cible;
    }

    /** Définit le nom ou identifiant du client cible. */
    public void setClient_cible(String client_cible) {
        this.client_cible = client_cible;
    }

    /** @return Le nom ou identifiant du client source. */
    public String getClient_source() {
        return client_source;
    }

    /** Définit le nom ou identifiant du client source. */
    public void setClient_source(String client_source) {
        this.client_source = client_source;
    }

    /** @return Les données utiles transportées par la trame. */
    public String getDu() {
        return du;
    }

    /** Définit les données utiles transportées par la trame. */
    public void setDu(String du) {
        this.du = du;
    }
}