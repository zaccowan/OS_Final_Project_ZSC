import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {
    //Stores all the clients that have been created.
    public static ArrayList<Client> clientList = new ArrayList<Client>();

    Socket socket;
    PrintWriter pr;
    BufferedReader br;
    String username;

    public Client() throws UnknownHostException, IOException {
        socket = new Socket("localhost", 8001);
        pr = new PrintWriter(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(System.in));


        try {
            System.out.println("Enter a username.");
            username = br.readLine();
            pr.println(username);
            pr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket.isConnected()) {
            System.out.println("Hello "+ username + ", enter message to send");
            try {
                String message;
                message = br.readLine();
                pr.println(message);
                pr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args) throws IOException {

        clientList.add(new Client());

    }//closes main()


}//Closes Class


