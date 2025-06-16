

import java.net.Inet4Address;
import java.util.ArrayList;

public class Table_routage {

    private ArrayList<String> serveurs;
    private ArrayList<Inet4Address> passerelles;
    private ArrayList<ArrayList<String>> clients_serveurs;
    private ArrayList<Integer> distance;

    public Table_routage() {
        serveurs = new ArrayList<String>();
        passerelles = new ArrayList<Inet4Address>();
        clients_serveurs = new ArrayList<ArrayList<String>>();
        distance = new ArrayList<Integer>();
    }

    public boolean addTable(Trame_routage trame, Inet4Address adresseSource) {

        // Serveurs et clients à rajouter
        ArrayList<String> tempServeurs = trame.getServeurs();
        ArrayList<ArrayList<String>> tempClientsServeurs = trame.getClients_serveurs();
        ArrayList<Integer> tempDistance = trame.getDistance();
        boolean res = false;

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
        }

        System.out.println("----- FIN TRAITEMENT DE LA TRAME DE ROUTAGE -----");
        return res;
    }

    public Trame_routage exportTable(String serveur_cible, String serveur_source) {
        Trame_routage trame = new Trame_routage(1, serveur_cible, serveur_source, serveurs, passerelles,
                clients_serveurs, distance);
        return trame;
    }

    public ArrayList<String> getServeurs() {
        return serveurs;
    }

    public void setServeurs(ArrayList<String> serveurs) {
        this.serveurs = serveurs;
    }

    public ArrayList<Inet4Address> getPasserelles() {
        return passerelles;
    }

    public void setPasserelles(ArrayList<Inet4Address> passerelles) {
        this.passerelles = passerelles;
    }

    public ArrayList<ArrayList<String>> getClients_serveurs() {
        return clients_serveurs;
    }

    public void setClients_serveurs(ArrayList<ArrayList<String>> clients_serveurs) {
        this.clients_serveurs = clients_serveurs;
    }

    public ArrayList<Integer> getDistance() {
        return distance;
    }

    public void setDistance(ArrayList<Integer> distance) {
        this.distance = distance;
    }

}