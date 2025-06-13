import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class LocalIP {

    private String privateIP;

    public LocalIP() {
    }

    // Renvoie l'adresse IP locale de la machine
    public String getLocalIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
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

    // Renvoie l'adresse IP locale d'un serveur distant à partir de son nom ou IP
    public String getServerLocalIP(String serverHost) {
        try {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            return serverAddress.getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public void setPublicIP(String publicIP) {
        this.privateIP = publicIP;
    }
}
