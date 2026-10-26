package com.mycompany.registration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * QuickChat entry point: Part 1 registration/login followed by the Part 2 menu.
 * Run this class in NetBeans. All menu input is read as text first to avoid crashes.
 */
public final class Registration {
    private Registration() { }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Login login = new Login();

        System.out.println("--- Registration ---");
        String firstName = readNonBlank(input, "Enter first name: ");
        String lastName = readNonBlank(input, "Enter last name: ");

        String username;
        do {
            System.out.print("Enter username: ");
            username = input.nextLine();
            if (!login.checkUserName(username)) {
                System.out.println("Username is not correctly formatted; please ensure that "
                        + "your username contains an underscore and is no more than five characters in length.");
            }
        } while (!login.checkUserName(username));
        System.out.println("Username successfully captured.");

        String password;
        do {
            System.out.print("Enter password: ");
            password = input.nextLine();
            if (!login.checkPasswordComplexity(password)) {
                System.out.println("Password is not correctly formatted; please ensure that "
                        + "the password contains at least eight characters, a capital letter, "
                        + "a number, and a special character.");
            }
        } while (!login.checkPasswordComplexity(password));
        System.out.println("Password successfully captured.");

        String phone;
        do {
            System.out.print("Enter cell phone number (for example +27718693002): ");
            phone = input.nextLine().trim();
            if (!login.checkCellPhoneNumber(phone)) {
                System.out.println("Cell phone number incorrectly formatted or does not contain an international code.");
            }
        } while (!login.checkCellPhoneNumber(phone));
        System.out.println("Cell phone number successfully added.");

        System.out.println(login.registerUser(username, password, firstName, lastName, phone));
        System.out.println("\n--- Login ---");
        System.out.print("Enter username: ");
        String loginUsername = input.nextLine();
        System.out.print("Enter password: ");
        String loginPassword = input.nextLine();
        boolean loggedIn = login.loginUser(loginUsername, loginPassword);
        System.out.println(login.returnLoginStatus(loggedIn));
        if (!loggedIn) {
            System.out.println("QuickChat cannot be accessed without logging in.");
            return;
        }
        runQuickChat(input, Path.of("messages.json"));
    }

    /** Default entry to the chat menu for callers who already have a Scanner. */
    public static void runQuickChat(Scanner input) {
        runQuickChat(input, Path.of("messages.json"));
    }

    /** Separate storage path makes it possible to test the console without changing real files. */
    public static void runQuickChat(Scanner input, Path storagePath) {
        QuickChat chat = new QuickChat(storagePath);
        System.out.println("Welcome to QuickChat.");
        int limit = readInt(input, "How many messages would you like to enter? ", 0, 10000);
        int entered = 0;
        boolean running = true;

        // Required while loop: only Option 3 exits the main menu.
        while (running) {
            System.out.println("\n1. Send Messages");
            System.out.println("2. Show recently sent messages (Coming Soon.)");
            System.out.println("3. Quit");
            int option = readInt(input, "Choose option: ", 1, 3);

            switch (option) {
                case 1:
                    if (entered >= limit) {
                        System.out.println("You have already entered all " + limit + " messages.");
                        break;
                    }
                    // Required for loop: number messages from zero in the hash, and one on screen.
                    for (; entered < limit; entered++) {
                        System.out.println("Message " + (entered + 1) + " of " + limit);
                        System.out.print("Enter recipient cell number: ");
                        String recipient = input.nextLine().trim();
                        Message draft;
                        do {
                            System.out.print("Enter message (maximum 250 characters): ");
                            String content = input.nextLine();
                            draft = new Message(recipient, content, entered);
                            System.out.println(draft.checkMessageLength());
                        } while (!draft.isMessageLengthValid());

                        // Display the actual generated ID/hash for THIS message.
                        System.out.println("Message ID generated: " + draft.getMessageID());
                        System.out.println("Message Hash generated: " + draft.getMessageHash());

                        boolean processed = false;
                        while (!processed) {
                            System.out.println("1. Send Message");
                            System.out.println("2. Disregard Message");
                            System.out.println("3. Store Message to send later");
                            int action = readInt(input, "Choose message action: ", 1, 3);
                            try {
                                if (action == 2) {
                                    System.out.println("Press 0 to delete the message.");
                                    int confirmation = readInt(input,
                                            "Enter 0 to discard or 1 to cancel: ", 0, 1);
                                    if (confirmation == 0) {
                                        draft.SentMessage(2, chat);
                                        System.out.println("Message discarded.");
                                        processed = true;
                                    }
                                } else {
                                    // Invalid numbers can be discarded, but not sent or stored.
                                    while (!draft.isRecipientValid()) {
                                        System.out.println(draft.checkRecipientCell());
                                        System.out.print("Enter corrected recipient number: ");
                                        draft = draft.withRecipient(input.nextLine().trim());
                                    }
                                    System.out.println(draft.checkRecipientCell());
                                    if (action == 1) {
                                        System.out.println(draft.SentMessage(1, chat));
                                        // Required display order: ID, Hash, Recipient, Message.
                                        System.out.println(draft.printMessages());
                                    } else {
                                        System.out.println(draft.SentMessage(3, chat));
                                    }
                                    processed = true;
                                }
                            } catch (IOException | IllegalArgumentException error) {
                                System.out.println("Message could not be processed: " + error.getMessage());
                                // Keep the draft available so the user can choose again.
                            }
                        }
                    }
                    System.out.println("Total messages successfully sent: " + chat.returnTotalMessages());
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    System.out.println(chat.printMessages());
                    System.out.println("Total messages successfully sent: " + chat.returnTotalMessages());
                    System.out.println("Goodbye from QuickChat.");
                    running = false;
                    break;
                default:
                    // readInt already guarantees choices 1..3.
                    break;
            }
        }
    }

    private static String readNonBlank(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = input.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("Please enter a value.");
        }
    }

    private static int readInt(Scanner input, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String text = input.nextLine().trim();
            try {
                int choice = Integer.parseInt(text);
                if (choice >= min && choice <= max) return choice;
            } catch (NumberFormatException ignored) {
                // Letter input is not a valid number; request input again.
            }
            System.out.println("Please enter a whole number from " + min + " to " + max + ".");
        }
    }
}
