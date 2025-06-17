import java.io.Serializable;

/**
 * Classe abstraite représentant une trame générique échangée dans le réseau.
 * Sert de base pour les trames de routage et de message.
 * <p>
 * Les types de messages possibles sont :
 * <ul>
 *   <li>0 — Création d’un serveur</li>
 *   <li>1 — Échange de table de routage</li>
 *   <li>2 — Mort d’un serveur</li>
 *   <li>3 — Création d’un client</li>
 * </ul>
 * </p>
 */
public abstract class Trame implements Serializable {

    private static final long serialVersionUID = -484492464833561910L;

    /** Type du message (voir la liste des types ci-dessus). */
    private int type_message;

    /** Nom ou IP du serveur destinataire. */
    private String serveur_cible;

    /** Nom ou IP du serveur source. */
    private String serveur_source;

    /**
     * Construit une nouvelle trame générique.
     *
     * @param type_message   Type du message (entier).
     * @param serveur_cible  Nom ou IP du serveur destinataire.
     * @param serveur_source Nom ou IP du serveur source.
     */
    public Trame(int type_message, String serveur_cible, String serveur_source) {
        this.type_message = type_message;
        this.serveur_cible = serveur_cible;
        this.serveur_source = serveur_source;
    }

    /** @return Le type du message. */
    public int getType_message() {
        return type_message;
    }

    /** Définit le type du message. */
    public void setType_message(int type_message) {
        this.type_message = type_message;
    }

    /** @return Le nom ou IP du serveur destinataire. */
    public String getServeur_cible() {
        return serveur_cible;
    }

    /** Définit le nom ou IP du serveur destinataire. */
    public void setServeur_cible(String serveur_cible) {
        this.serveur_cible = serveur_cible;
    }

    /** @return Le nom ou IP du serveur source. */
    public String getServeur_source() {
        return serveur_source;
    }

    /** Définit le nom ou IP du serveur source. */
    public void setServeur_source(String serveur_source) {
        this.serveur_source = serveur_source;
    }
}