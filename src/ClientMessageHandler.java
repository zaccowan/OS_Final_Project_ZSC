import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    private static boolean criticalSectionOpen;
    PrintWriter pr;
    private InputStreamReader isr;
    private BufferedReader br;
    private Socket socket;
    private String username = null;
    private String userMessage = null;
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
                    userMessage = br.readLine();
                    System.out.println(clientData.getUsername().toUpperCase() + ":  " + userMessage);
                }
                while(criticalSectionOpen) {}
                if(!criticalSectionOpen) {
                    criticalSectionOpen = true;
                    Server.addClientToList(clientData);
                    for(ClientData client : Server.getClientList()) {
                        if(!client.getUsername().equals(this.username) && userMessage != null ) {
                            pr = new PrintWriter(client.getSocket().getOutputStream());
                            pr.println(this.username + ": " + userMessage);
                            pr.flush();
                            System.out.println("[SERVER] Message sent to " + client.getUsername()
                                    + "(" + client.getSocket().getPort() + ")"
                                    + " by " + username+ ":\t"+ userMessage);
                        }
                    }
                    criticalSectionOpen = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}