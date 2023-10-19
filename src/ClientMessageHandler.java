import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    InputStreamReader isr;
    BufferedReader br;
    Socket socket;
    String username = null;

    public ClientMessageHandler(Socket s) throws IOException {
        this.socket = s;
        isr = new InputStreamReader(s.getInputStream());
        br = new BufferedReader(isr);
    }

    @Override
    public void run() {
        while(socket.isConnected()) {
            try {
                if( username == null) {
                    username = br.readLine();
                }
                System.out.println(username.toUpperCase() + ":  " + br.readLine());
//				System.out.println( "Client #" + socket.getPort() + ":   " + br.readLine());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}