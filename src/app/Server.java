package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message response;
            String username;
            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                response = connection.receive();
                username = response.getData();
            } while(response.getType() != MessageType.USER_NAME
                    || username == null
                    || username.equals("")
                    || connectionMap.containsKey(username));

            connectionMap.put(username, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));

            return username;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for(Map.Entry<String, Connection> pairs : connectionMap.entrySet()) {
                if(!pairs.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, pairs.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while(true) {
                Message message = connection.receive();

                if(message.getType() == MessageType.TEXT) {
                    Message message1 = new Message(MessageType.TEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(message1);
                } else {
                    ConsoleHelper.writeMessage("Error! Message type is not Text.");
                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Connection with " + socket.getRemoteSocketAddress() + " has been estabilished.");
            try {
                Connection connection = new Connection(socket);
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Enter the Server Port");
        int serverPort = ConsoleHelper.readInt();

        try(ServerSocket socket = new ServerSocket(serverPort)) {
            System.out.println("Server is running.");
            while(true) {
                Handler handler = new Handler(socket.accept());
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for(Map.Entry<String, Connection> con : connectionMap.entrySet()) {
            try {
                con.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Message couldn't be send");
            }
        }
    }
}
