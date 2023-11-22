import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws IOException {

        final int NUM_CLIENTS = 2;
        ExecutorService clientExecutor;
        ExecutorService serverExecutor;

        clientExecutor = Executors.newFixedThreadPool(NUM_CLIENTS);
        try {
            serverExecutor = Executors.newSingleThreadExecutor();
            serverExecutor.execute(new Server(NUM_CLIENTS));
            for( int i = 0 ; i < NUM_CLIENTS ; i++ ) {
                clientExecutor.submit(new Client());

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}