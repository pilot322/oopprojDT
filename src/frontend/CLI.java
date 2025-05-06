package frontend;

import java.util.Scanner;

import models.users.User;
import system.BankSystem;

public class CLI {
    public static Scanner input = new Scanner(System.in);
    public static BankSystem system = new BankSystem();

    public static void main(String[] args) {

        User user = promptToAuthenticate();

        while (true) {
            int choice = getMainMenuChoice(user);
            handleChoice(user, choice);
        }
    }

    private static void handleChoice(User user, int choice) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleChoice'");
    }

    private static int getMainMenuChoice(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMainMenuChoice'");
    }

    private static User promptToAuthenticate() {
        // System.out.println("1. Register\n2. Log in"); 
        // int choice = input.nextInt();
        // if(choice == 1){
        //     System.out.println("username (max ...): ");
        //     String username = input.next();

        //     System.out.println("password (max ...): ");
        //     String password = input.next();

        //     // system.register(type, username, password, );

        // }
        // TODO!
        return null;

    }
}
