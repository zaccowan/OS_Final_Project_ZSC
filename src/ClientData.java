import java.net.Socket;

public class ClientData {

    private Socket socket;
    private String username;

    public ClientData(Socket clientSocket, String clientUsername) {
        this.socket = clientSocket;
        this.username = clientUsername;
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
