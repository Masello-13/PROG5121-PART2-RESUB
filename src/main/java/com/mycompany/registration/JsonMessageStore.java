package com.mycompany.registration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Writes stored messages as one valid JSON array, preserving earlier entries. */
final class JsonMessageStore {
    private final Path path;

    JsonMessageStore(Path path) {
        this.path = path.toAbsolutePath();
    }

    void append(Message message) throws IOException {
        Path parent = path.getParent();
        Files.createDirectories(parent);

        String oldContent = Files.exists(path)
                ? Files.readString(path, StandardCharsets.UTF_8).trim()
                : "[]";
        if (!oldContent.startsWith("[") || !oldContent.endsWith("]")) {
            throw new IOException("Storage file is not a JSON array; no data was overwritten.");
        }
        String inner = oldContent.substring(1, oldContent.length() - 1).trim();
        String combined = "[\n" + (inner.isEmpty() ? "" : inner + ",\n")
                + toJson(message) + "\n]\n";

        Path temp = Files.createTempFile(parent, "quickchat-", ".tmp");
        try {
            Files.writeString(temp, combined, StandardCharsets.UTF_8);
            try {
                Files.move(temp, path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException notSupported) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static String toJson(Message m) {
        return "  {\n"
                + "    \"messageID\": \"" + escape(m.getMessageID()) + "\",\n"
                + "    \"recipientCell\": \"" + escape(m.getRecipientCell()) + "\",\n"
                + "    \"messageLength\": " + m.getMessageLength() + ",\n"
                + "    \"messageHash\": \"" + escape(m.getMessageHash()) + "\",\n"
                + "    \"message\": \"" + escape(m.getMessage()) + "\"\n"
                + "  }";
    }

    private static String escape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                case '\b': result.append("\\b"); break;
                case '\f': result.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }
}
