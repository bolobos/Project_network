import java.io.Serializable;
import java.util.ArrayList;

public class Trame_routage implements Serializable {

    public class ServerDef {

        public ArrayList<Client> listClientsDef = new ArrayList<>();
        public String adressServer;
        
        public ServerDef(){

        }

        public ServerDef(String adressServer, ArrayList<Client> listClientsDef) {
            this.adressServer = adressServer;
            if (listClientsDef != null) {
            this.listClientsDef = listClientsDef;
            }
        }
    }

    public ArrayList<ServerDef> listServerDefs = new ArrayList<>();

    public Trame_routage(){
        
    }

    public Trame_routage(ArrayList<ServerDef> listServerDefs) {
        this.listServerDefs = listServerDefs;
    }

}
