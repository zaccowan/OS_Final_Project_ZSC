import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Runnable {

    private static String serverName = "Computer Engineers";
    private static boolean serverNameCriticalOpen = false;
    private static final LinkedBlockingQueue<Socket> editServerQueue = new LinkedBlockingQueue<>();
    private static final ArrayList<Socket> socketList = new ArrayList<>();
    private static final ArrayList<ClientMessageHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService messageReceiverExecutor;

    public Server(int numClients) {
        messageReceiverExecutor = Executors.newFixedThreadPool(numClients);
    }

    @Override
    public void run() {
        System.out.println("[SERVER STARTED]");
        ServerSocket server;
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

    // Returns true if server name critical section was closed and successfully written to
    // Returns false if server name critical section was open and unable to be written to
    public static boolean isServerNameCriticalOpen() {
        return serverNameCriticalOpen;
    }
    public static void openServerNameCritical() {
        serverNameCriticalOpen = true;
    }
    public static void closeServerNameCritical() {
        serverNameCriticalOpen = false;
    }
    public static void setServerName(String newName) {
        if( !serverNameCriticalOpen) {
            serverName = newName.substring(0, 30);
            closeServerNameCritical();
        }
    }

    public static String getServerWelcomeMessage() {
        Date date = new Date();
        return "-------- -------- -------- -------- -------- --------\n"
                + "Welcome to the " + serverName + " Server!\n"
                + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() +  " - "
                + socketList.size() + " clients are connected.\n"
                + "-------- -------- -------- -------- -------- --------";
    }

    public static ArrayList<Socket> getSocketList() {
        return socketList;
    }
    public static ArrayList<ClientMessageHandler> getClientHandlerList() {
        return clientHandlerList;
    }


    public static void addToEditServerQueue(Socket client) {
        editServerQueue.add(client);
    }
    public static void removeFromEditServerQueue(Socket client) {
        editServerQueue.remove(client);
    }

    public static boolean isNextToEdit(Socket socket) {
        if(editServerQueue.isEmpty()) return false;
        return editServerQueue.peek().equals(socket);
    }

}//closes Server
