import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static String serverName = "Computer Engineers";
    private static ArrayList<Socket> socketList = new ArrayList<Socket>();
    private static ArrayList<ClientData> clientList = new ArrayList<ClientData>();
    ExecutorService messageExecutor;

    public Server(int numClients) {
        messageExecutor = Executors.newFixedThreadPool(numClients);
    }

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
            //Prints the state of the server and the respective time
            System.out.println(getServerWelcomeMessage());


            //Accepts any clients created by the thread above (and technically any other request by browsers, ...)
            Socket socket = null;
            try {
                socket = server.accept();
                messageExecutor.execute(new ClientMessageHandler(socket));
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

    public static void addClientToList(ClientData clientData) {
        clientList.add(clientData);
    }
    public static ArrayList<ClientData> getClientList() {
        return clientList;
    }
}//closes Server
