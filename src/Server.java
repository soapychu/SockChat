import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {

    public static int port;
    public static List<Client> clients = new Vector<>();

    public static void main(String[] args) {
        if (args.length == 0) {
            port = 1024;
        }
        else {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                System.out.println("Please specify a port number between 1024 and 65535.");
                System.exit(1);
            }
        }

        System.out.println("Server started. Listening on port " + port + ".");
        run();
    }

    public static void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                new Thread(new Client(client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}