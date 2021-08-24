package app.client;

import app.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    public class BotSocketThread extends Client.SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            String helloWorld = "Hello, there. " +
                    "I'm a bot. " +
                    "I understand the following commands: " +
                    "date, day, month, year, time, hour, minutes, seconds.";
            BotClient.this.sendTextMessage(helloWorld);

            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            String usernameString = "";
            boolean validRequest = false;

            ConsoleHelper.writeMessage(message);
            String[] tab = message.split(": ");
            if(tab.length != 2) {
                return;
            }
            usernameString += "Information for " + tab[0] + ": ";



            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat;
            String pattern = "";

            switch (tab[1]) {
                case "date": {
                    pattern = "d.MM.yyyy";
                    validRequest = true;
                    break;
                }
                case "day": {
                    pattern = "d";
                    validRequest = true;
                    break;
                }
                case "month": {
                    pattern = "MMMM";
                    validRequest = true;
                    break;
                }
                case "year": {
                    pattern = "yyyy";
                    validRequest = true;
                    break;
                }
                case "time": {
                    pattern = "H:mm:ss";
                    validRequest = true;
                    break;
                }
                case "hour": {
                    pattern = "H";
                    validRequest = true;
                    break;
                }
                case "minutes": {
                    pattern = "m";
                    validRequest = true;
                    break;
                }
                case "seconds": {
                    pattern = "s";
                    validRequest = true;
                    break;
                }
            }

            if(validRequest) {
                simpleDateFormat = new SimpleDateFormat(pattern);
                BotClient.this.sendTextMessage(usernameString + simpleDateFormat.format(calendar.getTime()));
            }
        }
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getUserName() {
        int x =  (int)(Math.random() * 100);

        return "date_bot_" + x;
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
