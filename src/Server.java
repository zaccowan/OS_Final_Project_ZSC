import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Threaded server class that stores server details and client information.
 * Server is responsible for dispatching threads to manage clients messages.
 *
 * @author Zachary Cowan
 * @version 11 /1/2023 Fall/2023
 */
public class Server implements Runnable {

    /**
     * Server name.
     */
    private static String serverName = "Computer Engineers";


    /**
     * Flag to maintain mutual exclusion of server name.
     * This is my implementation of a critical section for a thread shared resource.
     */
    private static boolean serverNameCriticalOpen = false;

    /**
     * Queue used to store request to edit server name.
     * If empty, a request is added but immediately serviced.
     * If not empty, a request is serviced when a client is next up and critical section has been closed by the previous.
     */
    private static final LinkedBlockingQueue<Socket> editServerQueue = new LinkedBlockingQueue<>();

    /**
     * Stores socket once client request is accepted
     */
    private static final ArrayList<Socket> socketList = new ArrayList<>();

    /**
     * Stores instances of a client message handler object.
     */
    private static final ArrayList<ClientMessageHandler> clientHandlerList = new ArrayList<>();

    /**
     * Executor service used to delegate a new message handler and maintain a Fixed Thread Pool of specified capacity.
     */
    private final ExecutorService messageReceiverExecutor;

    public static void main(String[] args) {
        ExecutorService serverExecutor = serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.execute(new Server(5, "The Boys"));
    }

    /**
     * Instantiates a new Server with option to change default server name.
     * @param numClients Max Number of clients to allow.
     * @param name Name of server.
     */
    public Server(int numClients, String name) {
        messageReceiverExecutor = Executors.newFixedThreadPool(numClients);
        serverName = name;
    }
    /**
     * Instantiates a new Server
     * @param numClients Max Number of clients to allow.
     */
    public Server(int numClients) {
        messageReceiverExecutor = Executors.newFixedThreadPool(numClients);
    }

    /**
     * Execution loop for Server where:
     *      clients connection request are accepted,
     *      a message handler is delegated for a connected client,
     *      and the socket list is managed.
     */
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
     * As part of design, server name is only read if server critical is closed.
     * @return The current server name.
     */
    public static String getServerName() {
        return serverName;
    }

    /**
     * Get the state of the server name critical section
     * @return Returns true Server Name critical section is open. False otherwise.
     */
    public static boolean isServerNameCriticalOpen() {
        return serverNameCriticalOpen;
    }

    /**
     * Open server name critical section for editing.
     */
    public static void openServerNameCritical() {
        serverNameCriticalOpen = true;
    }

    /**
     * Close server name critical section when editing complete.
     */
    public static void closeServerNameCritical() {
        serverNameCriticalOpen = false;
    }

    /**
     * Sets server name.
     * @param newName The new server name.
     */
    public static void setServerName(String newName) {
        if( !serverNameCriticalOpen) {
            serverName = newName.substring(0, 30);
            closeServerNameCritical();
        }
    }

    /**
     * Gets server welcome message.
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
     * @return The current socket list.
     */
    public static ArrayList<Socket> getSocketList() {
        return socketList;
    }

    /**
     * Gets client handler list.
     * @return The current client handler list.
     */
    public static ArrayList<ClientMessageHandler> getClientHandlerList() {
        return clientHandlerList;
    }


    /**
     * Add to client to edit server request queue.
     * @param client Edit requesting client socket.
     */
    public static void addToEditServerQueue(Socket client) {
        editServerQueue.add(client);
    }

    /**
     * Remove client from edit server request queue.
     * @param client The client socket requesting to end request.
     */
    public static void removeFromEditServerQueue(Socket client) {
        editServerQueue.remove(client);
    }

    /**
     * Determine if a socket is next in the edit request queue.
     *
     * @param socket The client socket to check.
     * @return true if client socket is next up in queue. false, otherwise.
     */
    public static boolean isNextToEdit(Socket socket) {
        if(editServerQueue.isEmpty()) return false;
        return editServerQueue.peek().equals(socket);
    }

}//closes Server
