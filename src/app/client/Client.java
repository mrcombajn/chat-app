package app.client;

import app.Connection;
import app.ConsoleHelper;
import app.Message;
import app.MessageType;

import static app.MessageType.*;

import java.io.IOException;
import java.net.Socket;


public class Client {
    protected Connection connection;
    private volatile boolean clientConnected;

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            System.out.println("User " + userName + " has joined the chat.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            System.out.println("User " + userName + "has left the chat.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            Message incomingMessage;
            MessageType msgType;
            do {
                incomingMessage = connection.receive();
                msgType = incomingMessage.getType();

                if(msgType == NAME_REQUEST) {
                    connection.send(new Message(USER_NAME, getUserName()));
                } else if(msgType == NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            } while(msgType == NAME_REQUEST);

        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message incomingMessage;
            MessageType msgType;

            do {
                incomingMessage = connection.receive();
                msgType = incomingMessage.getType();

                if(msgType == TEXT) {
                    processIncomingMessage(incomingMessage.getData());
                } else if(msgType == USER_ADDED) {
                    informAboutAddingNewUser(incomingMessage.getData());
                } else if(msgType == USER_REMOVED) {
                    informAboutDeletingNewUser(incomingMessage.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            } while(msgType == TEXT
                    || msgType == USER_ADDED
                    || msgType == USER_REMOVED);

        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }
    }

    protected String getServerAddress() {
        System.out.println("Enter Server Address: ");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        System.out.println("Enter Server Port: ");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        System.out.println("Enter Username: ");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(TEXT, text));
        } catch (IOException e) {
            clientConnected = false;
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                wait();
            }
            if(clientConnected) {
                ConsoleHelper.writeMessage("Connection established. To exit, enter 'exit'.");
                while(clientConnected) {
                    String msg = ConsoleHelper.readString();
                    if(msg.equals("exit")) {
                        break;
                    }
                    if(shouldSendTextFromConsole()) {
                        sendTextMessage(msg);
                    }
                }
            } else {
                ConsoleHelper.writeMessage("An error occurred while working with the client.");
            }
        } catch (InterruptedException e) {
            notify();
            e.printStackTrace();
        }


    }
}
