package com.mycompany.registration;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuickChatTest {
    @TempDir Path temp;

    @Test
    void sentMessageIsRecordedAndPrintedInRequiredOrder() {
        QuickChat app = new QuickChat(temp.resolve("stored.json"));
        Message message = new Message("+27718693002", "Hello there", 0, "0012345678");
        assertEquals("Message successfully sent.", app.sendMessage(message));
        assertEquals(1, app.returnTotalMessages());
        String printed = app.printMessages();
        assertTrue(printed.indexOf("Message ID:") < printed.indexOf("Message Hash:"));
        assertTrue(printed.indexOf("Message Hash:") < printed.indexOf("Recipient:"));
        assertTrue(printed.indexOf("Recipient:") < printed.indexOf("Message:"));
        assertTrue(printed.contains("00:0:HELLOTHERE"));
    }

    @Test
    void multipleSentMessagesArePrintedAndCounted() {
        QuickChat app = new QuickChat(temp.resolve("stored.json"));
        app.sendMessage(new Message("+27718693002", "First message", 0, "0012345678"));
        app.sendMessage(new Message("+27718693002", "Second message", 1, "1112345678"));
        assertEquals(2, app.returnTotalMessages());
        assertTrue(app.printMessages().contains("First message"));
        assertTrue(app.printMessages().contains("Second message"));
    }

    @Test
    void discardedMessageDoesNotCountAsSent() {
        QuickChat app = new QuickChat(temp.resolve("stored.json"));
        Message message = new Message("08575975889", "Hi Keegan, did you receive the payment?",
                0, "1234567890");
        assertEquals("Press 0 to delete the message.", app.discardMessage(message));
        assertEquals(0, app.returnTotalMessages());
        assertEquals(1, app.getDiscardedMessages().size());
        assertEquals("No messages have been sent.", app.printMessages());
        assertFalse(Files.exists(temp.resolve("stored.json")));
    }

    @Test
    void storedMessagesStayInValidJsonArrayWithoutOverwriting() throws IOException {
        Path output = temp.resolve("messages.json");
        QuickChat firstRun = new QuickChat(output);
        assertEquals("Message successfully stored.",
                firstRun.storeMessage(new Message("+27718693002", "First \"quoted\" message", 0, "0012345678")));
        // A second service instance simulates a restart.
        QuickChat secondRun = new QuickChat(output);
        secondRun.storeMessage(new Message("+27718693002", "Backslash \\ and newline\nend", 0, "1112345678"));
        String json = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(json.trim().startsWith("["));
        assertTrue(json.trim().endsWith("]"));
        assertTrue(json.contains("First \\\"quoted\\\" message"));
        assertTrue(json.contains("Backslash \\\\ and newline\\nend"));
        assertTrue(json.contains("0012345678"));
        assertTrue(json.contains("1112345678"));
        assertEquals(0, firstRun.returnTotalMessages());
        assertEquals(0, secondRun.returnTotalMessages());
    }

    @Test
    void invalidMessageCannotBeSent() {
        QuickChat app = new QuickChat(temp.resolve("stored.json"));
        Message invalid = new Message("not-a-phone", "Hello", 0, "1234567890");
        assertThrows(IllegalArgumentException.class, () -> app.sendMessage(invalid));
        assertEquals(0, app.returnTotalMessages());
    }

    @Test
    void processedMessageCannotBeSentTwice() {
        QuickChat app = new QuickChat(temp.resolve("stored.json"));
        Message message = new Message("+27718693002", "Hello", 0, "1234567890");
        app.sendMessage(message);
        assertThrows(IllegalArgumentException.class, () -> app.sendMessage(message));
        assertEquals(1, app.returnTotalMessages());
    }

    @Test
    void duplicateIdIsRejectedEvenIfItBelongsToAnotherMessage() {
        QuickChat app = new QuickChat(temp.resolve("stored.json"));
        app.sendMessage(new Message("+27718693002", "First", 0, "0012345678"));
        assertThrows(IllegalArgumentException.class,
                () -> app.sendMessage(new Message("+27718693002", "Other", 1, "0012345678")));
        assertEquals(1, app.returnTotalMessages());
    }

    @Test
    void failedJsonWriteDoesNotMarkMessageAsStored() throws IOException {
        Path destinationIsDirectory = temp.resolve("messages.json");
        Files.createDirectories(destinationIsDirectory);
        QuickChat app = new QuickChat(destinationIsDirectory);
        Message draft = new Message("+27718693002", "Test", 0, "0012345678");
        assertThrows(IOException.class, () -> app.storeMessage(draft));
        assertEquals(Message.Status.DRAFT, draft.getStatus());
        assertEquals(0, app.getStoredMessages().size());
        assertEquals(0, app.returnTotalMessages());
    }
}
