package task.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import task.dto.chat_completion.ChatCompletion;
import task.dto.chat_completion.Choice;
import task.dto.chat_completion.Message;
import task.dto.chat_completion.ChatCompletionModel;
import task.utils.Constant;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class OpenAIChatCompletionClient {

    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final ChatCompletionModel model;
    private final String apiKey;
    private final boolean printRequestResponse;

    public OpenAIChatCompletionClient(ChatCompletionModel chatCompletionModel, String apiKey, boolean printRequestResponse) {
        this.model = chatCompletionModel;
        this.apiKey = checkApiKey(apiKey);
        this.printRequestResponse = printRequestResponse;
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    private String checkApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return apiKey;
    }

    public Message responseWithMessage(List<Message> messages) throws Exception {
        var request = createRequest(messages);
        if (printRequestResponse) {
            System.out.println("REQUEST: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        }

        HttpRequest httpRequest = generateRequest(request);
        String responseBody = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
        if (printRequestResponse) {
            System.out.println("RESPONSE: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(responseBody)));
        }

        ChatCompletion chatCompletion = mapper.readValue(responseBody, ChatCompletion.class);
        Choice choice = chatCompletion.choices().getFirst();

        return choice.message();
    }

    private Map<String, Object> createRequest(List<Message> messages) {
        return Map.of(
                "model", this.model.getValue(),
                "messages", messages
        );
    }

    private HttpRequest generateRequest(Map<String, Object> requestBody) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(Constant.OPEN_AI_CHAT_COMPLETION_API_URI)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();
    }
}