

import java.io.Serializable;

public abstract class Trame implements Serializable {

	// Types de messages
	// 0 — Cr´eation d’un serveur
	// 1 — Echange de table routage ´
	// 2 — Mort d’un serveur
	// 3 — Cr´eation d’un client

	private static final long serialVersionUID = -484492464833561910L;
	private int type_message;
	private String serveur_cible;
	private String serveur_source;
	

	public Trame(int type_message, String serveur_cible, String serveur_source) {
		this.type_message = type_message;
		this.serveur_cible = serveur_cible;
		this.serveur_source = serveur_source;
	}

	public int getType_message() {
		return type_message;
	}

	public void setType_message(int type_message) {
		this.type_message = type_message;
	}

	public String getServeur_cible() {
		return serveur_cible;
	}

	public void setServeur_cible(String serveur_cible) {
		this.serveur_cible = serveur_cible;
	}

	public String getServeur_source() {
		return serveur_source;
	}

	public void setServeur_source(String serveur_source) {
		this.serveur_source = serveur_source;
	}

}