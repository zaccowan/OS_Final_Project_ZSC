import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

/**
 * Threaded client message handler to:
 *      receive messages from a client socket,
 *      handle client commands,
 *      and edit Server name with respect to critical section.
 * A client message handler is delegated to a specific client, the principal client.
 * @author Zachary Cowan
 * @version 11 /1/2023
 * Fall/2023
 */
public class ClientMessageHandler implements Runnable{

    /**
     * Socket of principal client.
     */
    private final Socket socket;
    /**
     * Username of principal client.
     */
    private String username;


    /**
     * Sends messages through principal client socket output stream.
     */
    private final PrintWriter clientWriter;
    /**
     * Receives messages through pricniapl client socket input stream.
     */
    private final BufferedReader br;

    /**
     * Instantiates a new Client message handler.
     * @param socket The socket of the client.
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public ClientMessageHandler(Socket socket) throws IOException {
        this.socket = socket;
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
        clientWriter = new PrintWriter(socket.getOutputStream());
        sendServerResponse(Server.getServerName());
    }

    /**
     * Execution loop for ClientMessageHandler where:
     *      client messages are received,
     *      client commands are serviced,
     *      and server sends dialogs and responses to requests.
     */
    @Override
    public void run() {

        while(true) {
            try {
                // Gets line sent through the socket
                String userMessage = br.readLine();

                // Handles username setting.
                // First message sent from client is automatically prepended with /username
                if( userMessage.startsWith("/username") && userMessage.length() >= 10) {
                    // Sets initial username
                    if( username == null) {
                        username = userMessage.substring(10);
                        sendServerDialogToAll(username + " has connected to the server. Make sure to welcome them!");
                    } else {
                        sendServerDialogToAll(username + " has changed their username to: " +
                                userMessage.substring(10));
                        username = userMessage.substring(10);
                    }
                }
                //
                // Handle a request to check if a username is actively taken on server.
                else if (userMessage.startsWith("/checkUser")) {
                    String usernameCandidate = userMessage.substring(11);
                    boolean usernameTaken = false;
                    for(ClientMessageHandler clientHandler : Server.getClientHandlerList()) {
                        if (usernameCandidate.equals(clientHandler.getUsername())) {
                            usernameTaken = true;
                            break;
                        }
                    }
                    if( usernameTaken ) {
                        sendServerResponse("/userIsTaken " + usernameCandidate);
                    } else {
                        sendServerResponse("/userIsUnique " + usernameCandidate);
                    }
                }

                //
                // Handle a user quit.
                else if( userMessage.equals("/quit")) {
                    sendDisconnectToAll(Objects.requireNonNullElseGet(username, () -> "Client #" + socket.getPort()));

                    Server.getSocketList().remove(socket);
                    socket.close();
                    System.out.println("[SERVER] " + Server.getSocketList().size() + " remain in server." );
                    return;  // Returns out of while loop and effectively ends thread execution
                }
                //
                // Handle a user request to edit server name.
                // Adds clients to server edit request queue.
                else if( userMessage.startsWith("/server") && userMessage.length() == 7) {
                    System.out.println("[SERVER] " + username + " has requested to edit server name.");
                    Server.addToEditServerQueue(socket);
                    if( Server.isServerNameCriticalOpen() ) {
                        sendServerResponse("You have been added to wait list to edit server name. You may continue to send messages. " +
                                "Type \"/forget\" at any time to cancel server edit request.");
                    }

                }
                //
                // Handle request to forget server name edit request.
                // Removes client from server name edit request queue.
                else if (userMessage.startsWith("/forget") && userMessage.length() == 7 ) {
                    sendServerResponse("Ending server edit request.");
                    Server.removeFromEditServerQueue(socket);
                }
                //
                // Handles normal messages sent by client
                else {
                    sendMessageToAll(getUsername(), userMessage);
                }

                //
                // Logic to service edit request.
                // Maintains server name critical section.
                if( Server.isNextToEdit(socket) && !Server.isServerNameCriticalOpen() ) {
                    Server.removeFromEditServerQueue(socket);
                    Server.openServerNameCritical();
                    sendServerResponse("Enter message to change server name.");
                    String newServerName = br.readLine();
                    Server.setServerName(newServerName);
                    sendServerCommand("/servername " + newServerName);
                    System.out.println("[SERVER] " + username + " has changed server name to " + newServerName);
                    Server.closeServerNameCritical();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//closes run()


    /**
     * Send a user disconnect notification to all.
     * @param username Username of disconnecting principal client.
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public void sendDisconnectToAll(String username) throws IOException {
        System.out.println("[SERVER] " + username + " has disconnected.");
        for( Socket recipientSocket : Server.getSocketList() ) {
            if( !this.socket.equals(recipientSocket) ) {
                PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream());
                recipientWriter.println(username + " has disconnected.");
                recipientWriter.flush();
            }
        }
    }

    /**
     * Send message to all (except principal client).
     * @param username Username of sending client.
     * @param message  Message of sending client.
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public void sendMessageToAll(String username, String message) throws IOException {
        for( Socket recipientSocket : Server.getSocketList() ) {
            if( !this.socket.equals(recipientSocket) ) {
                System.out.println("[SERVER] sending message to " + recipientSocket.getPort()
                        + ": " + message );
                PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream());
                recipientWriter.println(username + ": " + message);
                recipientWriter.flush();
            }
        }
    }

    /**
     * Send formatted server dialog to all.
     * Used to let server communicate with clients.
     * @param message Server message.
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public void sendServerDialogToAll(String message) throws IOException {
        for( Socket recipientSocket : Server.getSocketList() ) {
            if( !this.socket.equals(recipientSocket) ) {
                PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream());
                System.out.println("[SERVER] " + message);
                recipientWriter.println("[SERVER] " + message);
                recipientWriter.flush();
            }
        }
    }

    /**
     * Send server command.
     * Used to send work commands to clients such as changing server name.
     * @param command Command to send to clients.
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public void sendServerCommand(String command) throws IOException {
        for( Socket recipientSocket : Server.getSocketList() ) {
            PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream());
            System.out.println(command);
            recipientWriter.println(command);
            recipientWriter.flush();
        }
    }

    /**
     * Send server response to a principal client after request is serviced.
     * @param response Response of serviced request.
     */
    public void sendServerResponse(String response) {
        clientWriter.println(response);
        clientWriter.flush();
    }

    /**
     * Gets username of principal client
     * @return Username of client.
     */
    public String getUsername() {
        return this.username;
    }

}