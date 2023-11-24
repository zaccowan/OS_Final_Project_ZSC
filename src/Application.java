import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application to spin up server and specified number of client objects.
 * Note: each client object is a single thread with a swing GUI, opening too many may crash your system.
 * @author Zachary Cowan
 * @version 11 /1/2023
 * Fall/2023
 */
public class Application {

    /**
     * Just to satisfy javadocs warning.
     * Does nothing.
     */
    Application() {}

    /**
     * The entry point of application.
     * @param args main args
     */
    public static void main(String[] args) {

        final int NUM_CLIENTS = 2;
        ExecutorService clientExecutor;
        ExecutorService serverExecutor;

        clientExecutor = Executors.newFixedThreadPool(NUM_CLIENTS);
        try {
            serverExecutor = Executors.newSingleThreadExecutor();
            serverExecutor.execute(new Server(NUM_CLIENTS, "The Boys"));
            for( int i = 0 ; i < NUM_CLIENTS ; i++ ) {
                clientExecutor.submit(new Client());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}