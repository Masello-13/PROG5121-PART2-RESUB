package com.mycompany.registration;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Integration tests for menu loops and the exact Part 2 message actions. */
class ConsoleFlowTest {
    @TempDir Path temp;

    @Test
    void menuRepeatsAndEntryCounterIncrementsForDiscardedAndStoredMessages() throws Exception {
        String input = String.join("\n",
                "2", // Coming Soon (menu must reappear)
                "1", // Enter messages
                "08575975889", "Hi Keegan, did you receive the payment?", "2", "0", // Discard
                "+27718693002", "Hi Mike, can you join us for dinner tonight?", "1", // Send
                "3", // Quit
                "");
        String output = runChat(input, temp.resolve("stored.json"), 2);
        assertTrue(output.contains("Coming Soon."));
        assertTrue(output.contains("Message 1 of 2"));
        assertTrue(output.contains("Message 2 of 2"));
        assertTrue(output.contains("Message discarded."));
        assertTrue(output.contains("Message successfully sent."));
        assertTrue(output.contains("Total messages successfully sent: 1"));
        assertTrue(output.contains("Message Hash generated:"));
        assertTrue(output.matches("(?s).*Message Hash generated: [0-9]{2}:1:HITONIGHT.*"));
        assertFalse(Files.exists(temp.resolve("stored.json")));
    }

    @Test
    void invalidMenuAndMessageInputsAreRetried() throws Exception {
        String input = String.join("\n",
                "not-a-number", "9", "1", // Invalid choices, then 1
                "000", "", "X".repeat(251), "Hello!", // Invalid recipient and messages
                "1", // Send
                "+27718693002", // Fix number
                "3", // Exit
                "");
        String output = runChat(input, temp.resolve("stored.json"), 1);
        assertTrue(output.contains("Please enter a whole number from 1 to 3."));
        assertTrue(output.contains("Please enter a message."));
        assertTrue(output.contains("Message exceeds 250 characters by 1"));
        assertTrue(output.contains("Enter corrected recipient number"));
        assertTrue(output.contains("Total messages successfully sent: 1"));
    }

    @Test
    void storingMessageCreatesJsonAndDoesNotIncrementSentTotal() throws Exception {
        Path path = temp.resolve("nested/messages.json");
        String input = String.join("\n", "1", "+27718693002", "Test store", "3", "3", "");
        String output = runChat(input, path, 1);
        assertTrue(output.contains("Message successfully stored."));
        assertTrue(output.contains("Total messages successfully sent: 0"));
        String contents = Files.readString(path);
        assertTrue(contents.contains("Test store"));
        assertTrue(contents.startsWith("["));
    }

    private static String runChat(String input, Path path, int count) {
        PrintStream original = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));
            Scanner scanner = new Scanner(count + "\n" + input);
            Registration.runQuickChat(scanner, path);
        } finally {
            System.setOut(original);
        }
        return capture.toString(StandardCharsets.UTF_8);
    }
}
