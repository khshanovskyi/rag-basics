package task.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import task.dto.embeddings.EmbeddingsModel;
import task.dto.embeddings.EmbeddingsResponseDto;
import task.utils.Constant;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class OpenAIEmbeddingsClient {

    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final EmbeddingsModel model;
    private final String apiKey;
    private final boolean printResponse;

    public OpenAIEmbeddingsClient(EmbeddingsModel model, String apiKey, boolean printResponse) {
        this.model = model;
        this.apiKey = checkApiKey(apiKey);
        this.printResponse = printResponse;
        this.mapper = new ObjectMapper();
        this.httpClient = createHttpClient();
    }

    private String checkApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return apiKey;
    }

    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    public EmbeddingsResponseDto getEmbeddings(String input) throws Exception {
        var request = generateRequestBody(input);
        HttpRequest httpRequest = generateRequest(request);

        String responseBody = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
        if (printResponse) {
            System.out.println("RESPONSE: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(responseBody)));
        }

        return mapper.readValue(responseBody, EmbeddingsResponseDto.class);
    }

    protected Map<String, Object> generateRequestBody(String input) {
        return Map.of(
                "model", EmbeddingsModel.OI_TEXT_EMBEDDINGS_3_LARGE.getValue(),
                "input", input,
                "dimensions", 1536
        );
    }

    protected HttpRequest generateRequest(Map<String, Object> requestBody) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(Constant.OPEN_AI_EMBEDDINGS_API_URI)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();
    }
}