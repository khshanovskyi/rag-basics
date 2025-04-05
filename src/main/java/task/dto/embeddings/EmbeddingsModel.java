package task.dto.embeddings;

import lombok.Getter;

@Getter
public enum EmbeddingsModel {
    OI_TEXT_EMBEDDINGS_3_SMALL("text-embedding-3-small"),
    OI_TEXT_EMBEDDINGS_3_LARGE("ttext-embedding-3-large"),
    ;

    private final String value;

    EmbeddingsModel(String value) {
        this.value = value;
    }

}
