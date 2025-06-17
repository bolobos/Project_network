import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Classe utilitaire pour récupérer les adresses IP locales et distantes.
 */
public class LocalIP {

    /**
     * Adresse IP privée (non utilisée dans les méthodes actuelles).
     */
    private String privateIP;

    /**
     * Constructeur par défaut.
     */
    public LocalIP() {
    }

    /**
     * Renvoie l'adresse IP locale (IPv4) de la machine, hors loopback.
     * @return L'adresse IP locale sous forme de chaîne, ou "Unknown" si non trouvée.
     */
    public String getLocalIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignore les interfaces loopback et inactives
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Retourne la première adresse IPv4 non loopback trouvée
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            return "Unknown";
        }
        return "Unknown";
    }

    /**
     * Renvoie l'adresse IP d'un serveur distant à partir de son nom ou IP.
     * @param serverHost Nom d'hôte ou adresse IP du serveur.
     * @return L'adresse IP du serveur, ou "Unknown" en cas d'erreur.
     */
    public String getServerLocalIP(String serverHost) {
        try {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            return serverAddress.getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Définit l'adresse IP publique (non utilisée dans les méthodes actuelles).
     * @param publicIP L'adresse IP publique à enregistrer.
     */
    public void setPublicIP(String publicIP) {
        this.privateIP = publicIP;
    }
}
