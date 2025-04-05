package task.dto.embeddings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmbeddingsResponseDto (List<EmbeddingData> data, String model){
}
