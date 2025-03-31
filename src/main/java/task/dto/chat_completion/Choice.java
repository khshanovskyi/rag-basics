package task.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Choice(
        int index,
        Message message,
        Object logprobs,
        @JsonProperty("finish_reason")
        String finishReason
) {}