package task.dto.chat_completion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenDetails(
        @JsonProperty("cached_tokens")
        int cachedTokens,
        @JsonProperty("audio_tokens")
        int audioTokens,
        @JsonProperty("reasoning_tokens")
        int reasoningTokens,
        @JsonProperty("accepted_prediction_tokens")
        int acceptedPredictionTokens,
        @JsonProperty("rejected_predictionTokens")
        int rejectedPredictionTokens
) {}