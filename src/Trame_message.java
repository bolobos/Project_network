

import java.util.ArrayList;

public class Trame_message extends Trame {

	private String client_cible;
	private String client_source;
	private String du;

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
	 * @param routingTable   La table de routage contenant les associations
	 *                       client-serveur.
	 * @param serveurCourant Le nom du serveur courant.
	 * @return Un code entier indiquant la destination du message :
	 *         <ul>
	 *         <li>1 : Message destiné au serveur courant.</li>
	 *         <li>2 : Message destiné à un client local (présent dans la table de
	 *         routage et associé au serveur courant).</li>
	 *         <li>3 : Message destiné à un autre serveur (le client cible n'est pas
	 *         local).</li>
	 *         <li>4 : Message destiné à un autre serveur (le serveur cible n'est
	 *         pas le serveur courant).</li>
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

	public String getClient_cible() {
		return client_cible;
	}

	public void setClient_cible(String client_cible) {
		this.client_cible = client_cible;
	}

	public String getClient_source() {
		return client_source;
	}

	public void setClient_source(String client_source) {
		this.client_source = client_source;
	}

	public String getDu() {
		return du;
	}

	public void setDu(String du) {
		this.du = du;
	}

}