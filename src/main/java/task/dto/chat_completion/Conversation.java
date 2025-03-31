package task.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains <b>id</b> (UUID) and list of <b>messages</b>
 */
public class Conversation {

    private final UUID id;
    private final List<Message> messages;

    public Conversation() {
        this.id = UUID.randomUUID();
        this.messages = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }
}
