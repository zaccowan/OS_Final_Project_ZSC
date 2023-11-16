import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMessageHandler implements Runnable{

    private final BufferedReader br;
    private final Socket socket;
    private String username;

    public ClientMessageHandler(Socket socket) throws IOException {
        this.socket = socket;
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
        PrintWriter pr = new PrintWriter(socket.getOutputStream());
        pr.println(Server.getServerName());
        pr.flush();
    }

    @Override
    public void run() {
        while(true) {
            try {

                //Client class requires a user enter username upon connecting to server.
                // Get and set given username for a given client handler
                if( username == null) {
                    username = br.readLine();
                }
                // Once a username is set, start accepting messages.
                else {
                    String userMessage = br.readLine();
                    // Handle User Commands
                    if( userMessage.equals("/quit")) {
                        System.out.println(username + " has disconnected.");
                        for( Socket recipientSocket : Server.getSocketList() ) {
                            if( !this.socket.equals(recipientSocket) ) {
                                System.out.println("[SERVER] sending disconnect to " + recipientSocket.getPort());
                                PrintWriter pr = new PrintWriter(recipientSocket.getOutputStream());
                                pr.println(this.username + " has disconnected.");
                                pr.flush();
                            }
                        }
                        Server.getSocketList().remove(socket);
                        socket.close();
                        return;
                    }
                    else if( userMessage.startsWith("/server") && userMessage.length() == 7) {
                        System.out.println("[SERVER] " + username + " has requested to edit server name.");
                        Server.addToEditServerQueue(socket);

                        if( Server.isServerNameCriticalOpen() ) {
                            // Wait until server name critical section is open.
                            PrintWriter pr = new PrintWriter(socket.getOutputStream());
                            pr.println("You have been added to wait list to edit server name. Type \"/forget\" to end request.");
                            pr.flush();
                            while( !Server.isNextToEdit(socket) || Server.isServerNameCriticalOpen() ) {
                                //Wait until a socket is next to edit the queue
                                // Check if user send /forget to end their server edit request
//                                String response = br.readLine();
//                                if(response.equals("/forget")) {
//                                    pr.println("Ending server edit request.");
//                                    pr.flush();
//                                    Server.removeFromEditServerQueue(socket);
//                                    break;
//                                }
                                if(Server.isNextToEdit(socket) && !Server.isServerNameCriticalOpen()) {
                                    break;
                                }
                            }
                        }
                        if( Server.isNextToEdit(socket) ) {
                            Server.removeFromEditServerQueue(socket);
                            Server.openServerNameCrical();
                            PrintWriter pr = new PrintWriter(socket.getOutputStream());
                            pr.println("Enter message to change server name.");
                            pr.flush();
                            String newServerName = br.readLine();
                            Server.setServerName(newServerName);
                            System.out.println("[SERVER] " + username + " has changed server name to " + newServerName);
                        }
                    }
                    else {
                        for( Socket recipientSocket : Server.getSocketList() ) {
                            if( !this.socket.equals(recipientSocket) ) {
                                System.out.println("[SERVER] sending message to " + recipientSocket.getPort()
                                        + ": " + userMessage );
                                PrintWriter pr = new PrintWriter(recipientSocket.getOutputStream());
                                pr.println(this.username + ": " + userMessage);
                                pr.flush();
                            }
                        }
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//closes run()

    public String getUsername() {
        return this.username;
    }

}