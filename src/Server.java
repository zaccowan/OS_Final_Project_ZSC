import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class Server implements Runnable {

    private static String serverName = "Computer Engineers";
    private static final ArrayList<Socket> socketList = new ArrayList<Socket>();
    private static final ArrayList<ClientMessageHandler> clientHandlerList = new ArrayList<ClientMessageHandler>();
    private final ExecutorService messageReceiverExecutor;

    public Server(int numClients) {
        messageReceiverExecutor = Executors.newFixedThreadPool(numClients);
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


            //Accepts Client request made in Client.java (and technically any other request by browsers, ...)
            Socket socket;
            try {
                socket = server.accept();
                ClientMessageHandler handler = new ClientMessageHandler(socket);
                messageReceiverExecutor.execute(handler);
                clientHandlerList.add(handler);

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

    public static ArrayList<Socket> getSocketList() {
        return socketList;
    }
    public static ArrayList<ClientMessageHandler> getClientHandlerList() {
        return clientHandlerList;
    }

}//closes Server
