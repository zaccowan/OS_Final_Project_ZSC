import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The type Server.
 */
public class Server implements Runnable {

    private static String serverName = "Computer Engineers";
    private static boolean serverNameCriticalOpen = false;
    private static final LinkedBlockingQueue<Socket> editServerQueue = new LinkedBlockingQueue<>();
    private static final ArrayList<Socket> socketList = new ArrayList<>();
    private static final ArrayList<ClientMessageHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService messageReceiverExecutor;

    /**
     * Instantiates a new Server.
     *
     * @param numClients the num clients
     */
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

    /**
     * Gets server name.
     *
     * @return the server name
     */
    public static String getServerName() {
        return serverName;
    }

    /**
     * Is server name critical open boolean.
     *
     * @return the boolean
     */
// Returns true if server name critical section was closed and successfully written to
    // Returns false if server name critical section was open and unable to be written to
    public static boolean isServerNameCriticalOpen() {
        return serverNameCriticalOpen;
    }

    /**
     * Open server name critical.
     */
    public static void openServerNameCritical() {
        serverNameCriticalOpen = true;
    }

    /**
     * Close server name critical.
     */
    public static void closeServerNameCritical() {
        serverNameCriticalOpen = false;
    }

    /**
     * Sets server name.
     *
     * @param newName the new name
     */
    public static void setServerName(String newName) {
        if( !serverNameCriticalOpen) {
            serverName = newName.substring(0, 30);
            closeServerNameCritical();
        }
    }

    /**
     * Gets server welcome message.
     *
     * @return the server welcome message
     */
    public static String getServerWelcomeMessage() {
        Date date = new Date();
        return "-------- -------- -------- -------- -------- --------\n"
                + "Welcome to the " + serverName + " Server!\n"
                + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() +  " - "
                + socketList.size() + " clients are connected.\n"
                + "-------- -------- -------- -------- -------- --------";
    }

    /**
     * Gets socket list.
     *
     * @return the socket list
     */
    public static ArrayList<Socket> getSocketList() {
        return socketList;
    }

    /**
     * Gets client handler list.
     *
     * @return the client handler list
     */
    public static ArrayList<ClientMessageHandler> getClientHandlerList() {
        return clientHandlerList;
    }


    /**
     * Add to edit server queue.
     *
     * @param client the client
     */
    public static void addToEditServerQueue(Socket client) {
        editServerQueue.add(client);
    }

    /**
     * Remove from edit server queue.
     *
     * @param client the client
     */
    public static void removeFromEditServerQueue(Socket client) {
        editServerQueue.remove(client);
    }

    /**
     * Is next to edit boolean.
     *
     * @param socket the socket
     * @return the boolean
     */
    public static boolean isNextToEdit(Socket socket) {
        if(editServerQueue.isEmpty()) return false;
        return editServerQueue.peek().equals(socket);
    }

}//closes Server
