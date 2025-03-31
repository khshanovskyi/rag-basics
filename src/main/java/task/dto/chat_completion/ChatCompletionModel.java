package task.dto;

import lombok.Getter;

@Getter
public enum ChatCompletionModel {
    GPT_35_TURBO("gpt-3.5-turbo"),
    GPT_4o_MINI("gpt-4o-mini"),
    GPT_4o("gpt-4o"),
    ;

    private final String value;

    ChatCompletionModel(String value) {
        this.value = value;
    }

}
