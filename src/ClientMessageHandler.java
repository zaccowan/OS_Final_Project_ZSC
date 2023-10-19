import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    private static boolean criticalSectionOpen;
    private InputStreamReader isr;
    private BufferedReader br;
    private Socket socket;
    private String username;
    private ClientData clientData;

    public ClientMessageHandler(Socket socket) throws IOException {
        clientData = new ClientData(socket, "User" + socket.getLocalPort());
        isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
        criticalSectionOpen = false;
    }

    @Override
    public void run() {
        while(true) {
            try {
                if( username == null) {
                    username = br.readLine();
                    clientData.setUsername(username);
                }else {
                    System.out.println(clientData.getUsername().toUpperCase() + ":  " + br.readLine());
                }
                while(criticalSectionOpen) {}
                if(!criticalSectionOpen) {
                    criticalSectionOpen = true;
                    Server.addClientToList(clientData);
                    criticalSectionOpen = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}