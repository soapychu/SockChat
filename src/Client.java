import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class Client implements Runnable {

    public Socket socket;
    public String username;
    public BufferedReader input;
    public PrintWriter output;
    public boolean connected = true;

    public Client(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            Server.clients.add(this);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }

        send("i Welcome to the chat server. Please enter a username.");

        while (true) {
            String tempUsername = null;
            try {
                tempUsername = input.readLine();
            } catch (IOException e) {
                close();
                return;
            }

            if (checkUsername(tempUsername)) {
                username = tempUsername;
                break;
            } else {
                send("! Username already taken. Please enter a different username.");
            }
        }

        System.out.println(username + " connected.");
        sendAll(username + " has joined the chat.");

        while (connected) {
            try {
                String message = input.readLine();

                if (message == null) {
                    close();
                    break;
                }

                if (!checkMsg(message))
                    continue;

                sendAll(createMsg(message));
                System.out.println(createMsg(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String createMsg(String message) {
        return String.format("<%s>: %s", username, message);
    }
    public void send(String message) {
        output.println(message);
    }

    public void sendAll(String message) {
        for (Client client : Server.clients) {
            client.send(message);
        }
    }

    public void close() {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Server.clients.remove(this);
        connected = false;
    }

    public boolean checkMsg(String message) {
        boolean passing = true;
        if (!Pattern.matches("^[a-zA-Z0-9 !@#$%^&*()_+{}|:\"<>?,./;'\\[\\]=\\-]*$", message)) {
            send("! Message contains invalid characters.");
            passing = false;
        }
        if (message.equals("")) {
            send("! Message cannot be empty.");
            passing = false;
        }
        if (Pattern.matches("[\s]$", message)) {
            send("! Message cannot be whitespace only.");
            passing = false;
        }
        if (message.length() > 100) {
            send("! Message cannot be longer than 100 characters.");
            passing = false;
        }
        return passing;
    }

    public static boolean checkUsername(String username) {
        for (Client client : Server.clients) {
            if (client.username != null && client.username.equals(username)) {
                return false;
            }
        }
        return true;
    }
}