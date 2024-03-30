package client;

import java.util.Scanner;

public class InputManager {
    private static final Scanner sc = new Scanner(System.in);

    public static int getInt() {
        try {
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.print("Error, please enter an integer: ");
            return getInt();
        }
    }

    public static String getString() {
        try {
            String s = sc.nextLine();
            if (s.isEmpty()) {
                System.out.print("Error, empty string, please try again: ");
                return getString();
            }

            return s;

        } catch (Exception e) {
            System.out.print("Error, please enter a string: ");
            return getString();
        }
    }
}
