import java.net.Socket;

public record ClientData(Socket clientSocket, String clientUsername) {

}
