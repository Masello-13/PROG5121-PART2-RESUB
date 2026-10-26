package com.mycompany.registration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Manages messages: sent, discarded, and stored are different outcomes. */
public final class QuickChat {
    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> discardedMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();
    private final JsonMessageStore jsonStore;
    private final Set<String> processedIDs = new HashSet<>();

    public QuickChat(Path storageFile) {
        jsonStore = new JsonMessageStore(storageFile);
    }

    public String sendMessage(Message message) {
        validateMessage(message);
        requireUnusedID(message);
        message.markSent();
        sentMessages.add(message);
        processedIDs.add(message.getMessageID());
        return message.SentMessage();
    }

    public String discardMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
        requireDraft(message);
        requireUnusedID(message);
        message.markDiscarded();
        discardedMessages.add(message);
        processedIDs.add(message.getMessageID());
        return message.SentMessage();
    }

    public String storeMessage(Message message) throws IOException {
        validateMessage(message);
        requireUnusedID(message);
        // Save successfully before changing status or adding to the collection.
        jsonStore.append(message);
        message.markStored();
        storedMessages.add(message);
        processedIDs.add(message.getMessageID());
        return message.SentMessage();
    }

    private static void validateMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
        requireDraft(message);
        if (!message.checkMessageID()) {
            throw new IllegalArgumentException("Message ID must contain ten digits.");
        }
        if (!message.isRecipientValid()) {
            throw new IllegalArgumentException(message.checkRecipientCell());
        }
        if (!message.isMessageLengthValid()) {
            throw new IllegalArgumentException(message.checkMessageLength());
        }
    }

    private static void requireDraft(Message message) {
        if (message.getStatus() != Message.Status.DRAFT) {
            throw new IllegalArgumentException("This message has already been processed.");
        }
    }

    private void requireUnusedID(Message message) {
        if (processedIDs.contains(message.getMessageID())) {
            throw new IllegalArgumentException("A message with this ID has already been processed.");
        }
    }

    /** The Part 2 printMessages() requirement: include ALL successfully sent messages. */
    public String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (i > 0) result.append(System.lineSeparator()).append(System.lineSeparator());
            result.append(sentMessages.get(i).printMessages());
        }
        return result.toString();
    }

    public int returnTotalMessages() {
        return sentMessages.size();
    }

    public List<Message> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    public List<Message> getDiscardedMessages() {
        return Collections.unmodifiableList(discardedMessages);
    }

    public List<Message> getStoredMessages() {
        return Collections.unmodifiableList(storedMessages);
    }
}
