import java.io.Serializable;
import java.util.ArrayList;

public class Trame_routage implements Serializable {

    public class ServerDef {

        public ArrayList<Client> listClientsDef = new ArrayList<>();
        public String adressServer;
    }

    public ArrayList<ServerDef> listServerDefs = new ArrayList<>();

    public Trame_routage(){
        
    }

}
