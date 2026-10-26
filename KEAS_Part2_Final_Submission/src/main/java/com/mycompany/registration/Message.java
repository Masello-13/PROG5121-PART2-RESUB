package com.mycompany.registration;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** A single QuickChat message, including the fields required in POE Part 2. */
public final class Message {
    public enum Status { DRAFT, SENT, DISCARDED, STORED }

    public static final int MAX_MESSAGE_LENGTH = 250;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> GENERATED_IDS = new HashSet<>();
    private static final Pattern ID_PATTERN = Pattern.compile("[0-9]{10}");
    // Use the international +27 format as Part 1 and 2 explicitly require a country code.
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+27[0-9]{9}");

    private final String messageID;
    private final String recipientCell;
    private final String message;
    private final int sequenceNumber;
    private final String messageHash;
    private Status status = Status.DRAFT;

    /** Normal constructor: use a randomly generated ten-digit ID. */
    public Message(String recipientCell, String message, int sequenceNumber) {
        this(recipientCell, message, sequenceNumber, generateMessageID());
    }

    /** Constructor with a supplied ID to support repeatable unit tests. */
    public Message(String recipientCell, String message, int sequenceNumber, String messageID) {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("Message counter cannot be negative.");
        }
        if (messageID == null || !ID_PATTERN.matcher(messageID).matches()) {
            throw new IllegalArgumentException("Message ID must contain exactly ten digits.");
        }
        this.recipientCell = recipientCell == null ? "" : recipientCell.trim();
        this.message = message == null ? "" : message;
        this.sequenceNumber = sequenceNumber;
        this.messageID = messageID;
        this.messageHash = createMessageHash();
    }

    public static synchronized String generateMessageID() {
        String value;
        do {
            StringBuilder id = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                id.append(RANDOM.nextInt(10));
            }
            value = id.toString();
        } while (!GENERATED_IDS.add(value));
        return value;
    }

    public boolean checkMessageID() {
        return ID_PATTERN.matcher(messageID).matches();
    }

    /** The rubric's success or failure feedback for the 250-character limit. */
    public String checkMessageLength() {
        if (message.trim().isEmpty()) {
            return "Please enter a message.";
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return "Message exceeds 250 characters by "
                    + (message.length() - MAX_MESSAGE_LENGTH)
                    + "; please reduce the size.";
        }
        return "Message ready to send.";
    }

    public boolean isMessageLengthValid() {
        return !message.trim().isEmpty() && message.length() <= MAX_MESSAGE_LENGTH;
    }

    public boolean isRecipientValid() {
        return PHONE_PATTERN.matcher(recipientCell).matches();
    }

    public String checkRecipientCell() {
        if (isRecipientValid()) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    /** ID prefix : zero-based message counter : FIRSTWORDLASTWORD (uppercase). */
    public String createMessageHash() {
        String firstWord = "";
        String lastWord = "";
        // Ignore tokens that contain punctuation but no letters or digits.
        for (String token : message.trim().split("\\s+")) {
            String word = cleanWord(token);
            if (!word.isEmpty()) {
                if (firstWord.isEmpty()) firstWord = word;
                lastWord = word;
            }
        }
        return messageID.substring(0, 2) + ":" + sequenceNumber + ":" + firstWord + lastWord;
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^\\p{L}\\p{N}]", "").toUpperCase(Locale.ROOT);
    }

    /** Method name mirrors the name used in the assignment. */
    public String SentMessage() {
        switch (status) {
            case SENT:
                return "Message successfully sent.";
            case DISCARDED:
                return "Press 0 to delete the message.";
            case STORED:
                return "Message successfully stored.";
            default:
                return "Message has not been processed.";
        }
    }

    /**
     * Implements the three Part 2 message choices.
     * 1 = send, 2 = disregard, 3 = store for later.
     * The menu gets the choice from the user and passes it here.
     */
    public String SentMessage(int choice, QuickChat quickChat) throws java.io.IOException {
        if (quickChat == null) {
            throw new IllegalArgumentException("QuickChat cannot be null.");
        }
        switch (choice) {
            case 1:
                return quickChat.sendMessage(this);
            case 2:
                return quickChat.discardMessage(this);
            case 3:
                return quickChat.storeMessage(this);
            default:
                throw new IllegalArgumentException("Choose 1 to send, 2 to disregard, or 3 to store.");
        }
    }

    void markSent() { status = Status.SENT; }
    void markDiscarded() { status = Status.DISCARDED; }
    void markStored() { status = Status.STORED; }

    public String printMessages() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipientCell + System.lineSeparator()
                + "Message: " + message;
    }

    /** Return a new draft retaining the ID when a phone number is corrected. */
    public Message withRecipient(String newRecipient) {
        return new Message(newRecipient, message, sequenceNumber, messageID);
    }

    public String getMessageID() { return messageID; }
    public String getRecipientCell() { return recipientCell; }
    public String getMessage() { return message; }
    public int getMessageLength() { return message.length(); }
    public int getSequenceNumber() { return sequenceNumber; }
    public String getMessageHash() { return messageHash; }
    public Status getStatus() { return status; }
}
