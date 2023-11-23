import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    private final BufferedReader br;
    private final Socket socket;
    private String username;
    private final PrintWriter clientWriter;

    public ClientMessageHandler(Socket socket) throws IOException {
        this.socket = socket;
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
        clientWriter = new PrintWriter(socket.getOutputStream());
        sendServerResponse(Server.getServerName());
    }

    @Override
    public void run() {

        while(true) {
            try {
                // Gets line sent through the socket
                String userMessage = br.readLine();

                // Handles username setting.
                // First message sent from client is automatically prepended with /username
                if( userMessage.startsWith("/username")) {
                    // Sets initial username
                    if( username == null) {
                        username = userMessage.substring(10, userMessage.length());
                        sendServerDialogToAll(username + " has connected to the server. Make sure to welcome them!");
                    } else {
                        sendServerDialogToAll(username + " has changed their username to: " +
                                userMessage.substring(10, userMessage.length()));
                        username = userMessage.substring(10, userMessage.length());
                    }
                } else if (userMessage.startsWith("/checkUser")) {
                    String usernameCandidate = userMessage.substring(11, userMessage.length());
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
                // Handle a user quit
                else if( userMessage.equals("/quit")) {
                    if( username == null)
                        sendDisconnectToAll("Client #" + String.valueOf(socket.getPort()));
                    else
                        sendDisconnectToAll(username);

                    Server.getSocketList().remove(socket);
                    socket.close();
                    System.out.println("[SERVER] " + Server.getSocketList().size() + " remain in server." );
                    return;  // Returns out of while loop and effectively ends thread execution

                }
                //
                // handle user edit server request
                else if( userMessage.startsWith("/server") && userMessage.length() == 7) {
                    System.out.println("[SERVER] " + username + " has requested to edit server name.");
                    Server.addToEditServerQueue(socket);
                    if( Server.isServerNameCriticalOpen() ) {
                        sendServerResponse("You have been added to wait list to edit server name. You may continue to send messages. " +
                                "Type \"/forget\" at any time to cancel server edit request.");
                    }

                } else if (userMessage.startsWith("/forget") && userMessage.length() == 7 ) {
                    sendServerResponse("Ending server edit request.");
                    Server.removeFromEditServerQueue(socket);
                }
                //
                // Handles normal messages sent by client
                else {
                    sendMessageToAll(getUsername(), userMessage);
                }


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

    public void sendServerCommand(String command) throws IOException {
        for( Socket recipientSocket : Server.getSocketList() ) {
            PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream());
            System.out.println(command);
            recipientWriter.println(command);
            recipientWriter.flush();
        }
    }

    public void sendServerResponse(String response) {
        clientWriter.println(response);
        clientWriter.flush();
    }

    public String getUsername() {
        return this.username;
    }

}