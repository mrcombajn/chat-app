package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        while(true) {
            try {
                String text = reader.readLine();
                return text;
            } catch (IOException e) {
                System.out.println("An error occurred while trying to enter text. Try again.");
            }
        }
    }

    public static int readInt() {
        while(true) {
            try {
                String text = readString();
                int number = Integer.parseInt(text);
                return number;
            } catch (NumberFormatException e) {
                System.out.println("An error while trying to enter a number. Try again.");
            }
        }
    }
}
