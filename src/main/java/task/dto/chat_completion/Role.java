package task.dto;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Contains constants for roles (system, user, assistant, etc)
 */
public enum Role {
    SYSTEM("system"),
    USER("user"),
    AI("assistant"),
    TOOL("tool");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
