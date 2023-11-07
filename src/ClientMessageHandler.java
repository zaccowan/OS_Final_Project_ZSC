import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    private final BufferedReader br;
    private final Socket socket;
    private String username;

    public ClientMessageHandler(Socket socket) throws IOException {
        this.socket = socket;
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
    }

    @Override
    public void run() {
        while(true) {
            try {
                if( username == null) {
                    username = br.readLine();
                } else {
                    String userMessage = br.readLine();
                    for( Socket recipientSocket : Server.getSocketList() ) {
                        if( this.socket.equals(recipientSocket) ) {
                            continue;
                        } else {
                            System.out.println("[SERVER] sending message to " + recipientSocket.getPort()
                                    + ": " + userMessage );
                            PrintWriter pr = new PrintWriter(recipientSocket.getOutputStream());
                            pr.println(this.username + ": " + userMessage);
                            pr.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//closes run()

    public String getUsername() {
        return this.username;
    }

}