import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

public class PublicIP {

    private String publicIP;



    public PublicIP() {}


    public String getPublicIP() {
        try {
            URI uri = new URI("https://api.ipify.org");
            URL url = uri.toURL();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            this.publicIP = in.readLine();
            //System.out.println("Public IP: " + publicIP);

            in.close();
        } catch (Exception e) {
            System.out.println("Could not get public IP: " + e.getMessage());
            publicIP="0";
        }

        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }
}
