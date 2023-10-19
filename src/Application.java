import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
    public static void main(String[] args) throws IOException {

        ExecutorService clientExecutor = Executors.newFixedThreadPool(2);
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.execute(new Server());
        clientExecutor.execute(new Client());
        clientExecutor.execute(new Client());

    }

}