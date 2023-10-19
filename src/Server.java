import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server implements Runnable {

    private static String serverName = "Computer Engineers";
    private static ArrayList<Socket> socketList = new ArrayList<Socket>();
    public static ArrayList<ClientData> clientList = new ArrayList<ClientData>();

    @Override
    public void run() {
        System.out.println("[SERVER STARTED]");
        ServerSocket server = null;
        try {
            server = new ServerSocket(8001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        while (true) {
            if(!clientList.isEmpty()) {
                for(ClientData c : clientList) {
                    System.out.println(c.clientUsername());
                }
            }
            //Prints the state of the server and the respective time
            System.out.println(getServerWelcomeMessage());


            //Accepts any clients created by the thread above (and technically any other request by browsers, ...)
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            socketList.add(socket);

            System.out.println("Client #" + socket.getPort() + " has connected.");


        } //closes while
    }

    public static String getServerName() {
        return serverName;
    }

    public static String getServerWelcomeMessage() {
        Date date = new Date();
        return "--------\n" + "Welcome to the " + serverName + " Server!\n" +
                + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() +  " - "
                + socketList.size() + " clients are connected.\n--------";
    }
}//closes Server
