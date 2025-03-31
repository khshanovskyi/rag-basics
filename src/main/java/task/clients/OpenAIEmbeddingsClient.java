package task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import task.dto.chat_completion.ChatCompletion;
import task.dto.chat_completion.Choice;
import task.dto.embeddings.EmbeddingsModel;
import task.dto.chat_completion.Message;
import task.dto.embeddings.EmbeddingsResponseDto;
import task.utils.Constant;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class OpenAIEmbeddingsClient {
    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final EmbeddingsModel model;
    private final String apiKey;

    public OpenAIEmbeddingsClient(EmbeddingsModel model, String apiKey) {
        this.model = model;
        this.apiKey = checkApiKey(apiKey);
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    private String checkApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return apiKey;
    }

    public EmbeddingsResponseDto getEmbeddings(String input) throws Exception {
        var request = createRequest(input);
        HttpRequest httpRequest = generateRequest(request);

        String responseBody = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
        System.out.println("RESPONSE: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(responseBody)));

        EmbeddingsResponseDto embeddingsResponse = mapper.readValue(responseBody, EmbeddingsResponseDto.class);

        return embeddingsResponse;
    }

    private Map<String, Object> createRequest(String input) {
        return Map.of(
                "model", this.model.getValue(),
                "input", input
        );
    }

    private HttpRequest generateRequest(Map<String, Object> requestBody) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(Constant.OPEN_AI_EMBEDDINGS_API_URI)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();
    }
}