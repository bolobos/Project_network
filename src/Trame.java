public class Trame {
    
    private int id_trame;
    private int id_client_src;
    private int id_client_dest;
    private int ip_server_src;
    private int ip_server_dest;
    private String data;


    

    public int getId_trame() {
        return id_trame;
    }
    public void setId_trame(int id_trame) {
        this.id_trame = id_trame;
    }
    public int getId_client_src() {
        return id_client_src;
    }
    public void setId_client_src(int id_client_src) {
        this.id_client_src = id_client_src;
    }
    public int getId_client_dest() {
        return id_client_dest;
    }
    public void setId_client_dest(int id_client_dest) {
        this.id_client_dest = id_client_dest;
    }
    public int getIp_server_src() {
        return ip_server_src;
    }
    public void setIp_server_src(int ip_server_src) {
        this.ip_server_src = ip_server_src;
    }
    public int getIp_server_dest() {
        return ip_server_dest;
    }
    public void setIp_server_dest(int ip_server_dest) {
        this.ip_server_dest = ip_server_dest;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    
}
