package task.dto.chat_completion;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains <b>id</b> (UUID) and list of <b>messages</b>
 */
@Getter
public class Conversation {

    private final UUID id;
    private final List<Message> messages;

    public Conversation() {
        this.id = UUID.randomUUID();
        this.messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }
}
