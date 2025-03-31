package task.dto.chat_completion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Usage(
        @JsonProperty("prompt_tokens")
        int promptTokens,
        @JsonProperty("completion_okens")
        int completionTokens,
        @JsonProperty("total_tokens")
        int totalTokens,
        @JsonProperty("prompt_tokens_details")
        TokenDetails promptTokensDetails,
        @JsonProperty("completion_tokens_details")
        TokenDetails completionTokensDetails
) {}

