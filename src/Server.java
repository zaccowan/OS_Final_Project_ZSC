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

public class Server {

    private static ArrayList<Socket> socketList = new ArrayList<Socket>();

    public static void main(String [] args) throws IOException {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("[SERVER STARTED]");
        ServerSocket server = new ServerSocket(8001);


        while (true) {
            //Prints the state of the server and the respective time
            Date date = new Date();
            System.out.println("--------\n"
                    + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() +  " - "
                    + socketList.size() + " clients are connected.\n--------");

            if(!socketList.isEmpty()) {
                for(Socket s: socketList) {
                    executor.execute(new ClientMessageHandler(s));
                }
            }


            //Accepts any clients created by the thread above (and technically any other request by browsers, ...)
            Socket socket = server.accept();

            socketList.add(socket);
            System.out.println("Client #" + socket.getPort() + " has connected.");


        } //closes while

    }//closes main()

}//closes Server
