import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    
    private static String serverName = "Computer Engineers";
    private static ArrayList<Socket> socketList = new ArrayList<Socket>();
    public static ArrayList<Client> clientList = new ArrayList<Client>();

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("[SERVER STARTED]");
        ServerSocket server = null;
        try {
            server = new ServerSocket(8001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        while (true) {
            //Prints the state of the server and the respective time
            Date date = new Date();
            System.out.println("--------\n" + "Welcome to the " + serverName + " Server!\n" +
                    + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() +  " - "
                    + socketList.size() + " clients are connected.\n--------");

            if(!socketList.isEmpty()) {
                for(Socket s: socketList) {
                    try {
                        executor.execute(new ClientMessageHandler(s));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


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
}//closes Server
