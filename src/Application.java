import javax.management.timer.Timer;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws IOException {

        final int NUM_CLIENTS = 2;

        try {
            ExecutorService clientExecutor = Executors.newFixedThreadPool(NUM_CLIENTS);
            ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
            serverExecutor.execute(new Server(NUM_CLIENTS));
            clientExecutor.submit(new Client());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}