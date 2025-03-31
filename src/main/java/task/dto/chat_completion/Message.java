package task.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private Role role;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tool_call_id")
    private String toolCallId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    public Message(Role role, String content) {
        this.role = role;
        this.content = content;
    }

}