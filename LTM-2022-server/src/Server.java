import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket server = null;
    private static Socket socket = null;
    public static Thread thread = null;

    public void start() {
        try {
            server = new ServerSocket(3000);
            System.out.println("Server is listening on port 3000...");
            while (!server.isClosed()) {
                socket = server.accept();
                System.out.println("Client " + socket.getInetAddress() + " connected...");
                ClientHandler client = new ClientHandler(socket);
                thread = new Thread(client);
                thread.start();
            }
        } catch (Exception error) {
            closeConnect();
        }
    }

    public void closeConnect() {
        try {
            if (socket != null)
                socket.close();
            if (server != null)
                server.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
