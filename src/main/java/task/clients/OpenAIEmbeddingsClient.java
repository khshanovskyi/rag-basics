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
        //todo: 1. Create request by calling `generateRequestBody(String input)`
        //todo: 2. Generate HttpRequest using `generateRequest(Map<String, Object> request)`
        //todo: 3. Send HTTP request and get response body (use `HttpResponse.BodyHandlers.ofString()`)
        //todo: 4. If `printResponse` is true, print the response to console (use `mapper.writerWithDefaultPrettyPrinter()`)
        //todo: 5. Parse the responseBody into EmbeddingsResponseDto using the ObjectMapper
        //todo: 6. Return the parsed response

        throw new RuntimeException("Not implemented");
    }

    protected Map<String, Object> generateRequestBody(String input) {
        //todo: Create and return a Map with parameters:
        //todo:     - "model": the model value from the class field *
        //todo:     - "input": the input text parameter *
        //todo:     - (Optional) "dimensions": the vector dimensions size. You can play with it to evaluate the search results

        throw new RuntimeException("Not implemented");
    }

    protected HttpRequest generateRequest(Map<String, Object> requestBody) throws JsonProcessingException {
        //todo: Build an HttpRequest that:
        //todo:     - Uses the OpenAI Embeddings API URI from Constants
        //todo:     - Adds Authorization header with the API key in format "Bearer {apiKey}"
        //todo:     - Sets Content-Type to "application/json"
        //todo:     - Creates a POST request with requestBody converted to JSON string
        //todo:     - Returns the built request

        throw new RuntimeException("Not implemented");
    }
}