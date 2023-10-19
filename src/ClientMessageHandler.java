import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    private InputStreamReader isr;
    private BufferedReader br;
    private Socket socket;
    private String username;
    private ClientData clientData;

    public ClientMessageHandler(Socket socket) throws IOException {
        clientData = new ClientData(socket, "User" + socket.getLocalPort());
        isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
    }

    @Override
    public void run() {
        while(true) {
            try {
                if( username == null) {
                    username = br.readLine();
                    clientData.setUsername(username);
                    Server.addClientToList(clientData);
                    System.out.println("Username: " + Server.getClientList().get(0).getUsername());
                }else {
                    System.out.println(clientData.getUsername().toUpperCase() + ":  " + br.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}