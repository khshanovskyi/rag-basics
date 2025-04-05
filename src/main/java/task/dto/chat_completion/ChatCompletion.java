package task.dto.chat_completion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatCompletion(
        long created,
        String model,
        List<Choice> choices
) {

}

