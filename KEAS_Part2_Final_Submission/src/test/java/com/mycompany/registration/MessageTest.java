package com.mycompany.registration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

class MessageTest {

    @Test
    void sentMessageChoiceIsProcessedByMessageMethod() throws java.io.IOException {
        java.nio.file.Path json = java.nio.file.Files.createTempFile("quickchat-actions", ".json");
        java.nio.file.Files.deleteIfExists(json);
        QuickChat app = new QuickChat(json);
        Message first = new Message("+27718693002", "Hello", 0, "0000000001");
        Message second = new Message("+27718693002", "Discard this", 1, "0000000002");
        Message third = new Message("+27718693002", "Store this", 2, "0000000003");
        assertEquals("Message successfully sent.", first.SentMessage(1, app));
        assertEquals("Press 0 to delete the message.", second.SentMessage(2, app));
        assertEquals("Message successfully stored.", third.SentMessage(3, app));
        assertEquals(1, app.returnTotalMessages());
        assertEquals(1, app.getDiscardedMessages().size());
        assertEquals(1, app.getStoredMessages().size());
        assertTrue(java.nio.file.Files.readString(json).contains("Store this"));
        java.nio.file.Files.deleteIfExists(json);
    }

    @Test
    void generatedMessageIdContainsExactlyTenDigits() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            Message message = new Message("+27718693002", "Hello!", 0);
            assertTrue(message.checkMessageID());
            assertTrue(message.getMessageID().matches("[0-9]{10}"));
            assertTrue(ids.add(message.getMessageID()));
        }
    }

    @Test
    void hashMatchesExampleInAssignment() {
        Message message = new Message("+27718693002",
                "Hi Mike, can you join us for dinner tonight?", 0, "0012345678");
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
        assertEquals("00:0:HITONIGHT", message.getMessageHash());
    }

    @Test
    void hashChangesWhenCounterChanges() {
        Message message = new Message("+27718693002", "Hello thanks", 3, "9933333333");
        assertEquals("99:3:HELLOTHANKS", message.getMessageHash());
    }

    @Test
    void messageLengthSuccessAndFailureAreDifferent() {
        Message exactly250 = new Message("+27718693002", "X".repeat(250), 0, "1234567890");
        Message tooLong = new Message("+27718693002", "X".repeat(257), 0, "1234567891");
        assertTrue(exactly250.isMessageLengthValid());
        assertEquals("Message ready to send.", exactly250.checkMessageLength());
        assertFalse(tooLong.isMessageLengthValid());
        assertEquals("Message exceeds 250 characters by 7; please reduce the size.",
                tooLong.checkMessageLength());
    }

    @Test
    void emptyMessageIsNotValid() {
        Message empty = new Message("+27718693002", "   ", 0, "1234567890");
        assertFalse(empty.isMessageLengthValid());
        assertEquals("Please enter a message.", empty.checkMessageLength());
    }

    @Test
    void recipientValidationAcceptsCorrectInternationalFormat() {
        Message international = new Message("+27718693002", "Hi", 0, "1234567890");
        assertTrue(international.isRecipientValid());
        assertEquals("Cell phone number successfully captured.", international.checkRecipientCell());
    }

    @Test
    void recipientValidationRejectsMalformedOrOverlongNumbers() {
        assertFalse(new Message("//+27718693002", "Hi", 0, "1234567890").isRecipientValid());
        assertFalse(new Message("08575975889", "Hi", 0, "1234567891").isRecipientValid());
        assertFalse(new Message("0718693002", "Hi", 0, "1234567894").isRecipientValid());
        assertFalse(new Message("123", "Hi", 0, "1234567892").isRecipientValid());
        assertFalse(new Message("0718-693-002", "Hi", 0, "1234567893").isRecipientValid());
    }

    @Test
    void idCannotBeTooShortOrContainLetters() {
        assertThrows(IllegalArgumentException.class,
                () -> new Message("0718693002", "Hi", 0, "1234"));
        assertThrows(IllegalArgumentException.class,
                () -> new Message("0718693002", "Hi", 0, "123456789A"));
    }

    @Test
    void twoMessageIndexesMustGenerateDifferentHashes() {
        Message first = new Message("+27718693002", "Hi tonight?", 0, "0012345678");
        Message second = new Message("+27718693002", "Hi tonight?", 1, "0012345679");
        assertEquals("00:0:HITONIGHT", first.getMessageHash());
        assertEquals("00:1:HITONIGHT", second.getMessageHash());
    }

    @Test
    void hashIgnoresOnlyPunctuationTokensAtTheEdges() {
        Message sample = new Message("+27718693002", "... Hi there !!!", 0, "0012345678");
        assertEquals("00:0:HITHERE", sample.getMessageHash());
    }
}
