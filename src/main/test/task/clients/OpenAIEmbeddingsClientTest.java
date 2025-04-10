package task.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import task.dto.embeddings.EmbeddingData;
import task.dto.embeddings.EmbeddingsModel;
import task.dto.embeddings.EmbeddingsResponseDto;
import task.utils.Constant;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenAIEmbeddingsClientTest {

    private OpenAIEmbeddingsClient client;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;
    private String validApiKey;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        validApiKey = "test-api-key";
        objectMapper = new ObjectMapper();

        // Create mock HTTP client and response
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockResponse = Mockito.mock(HttpResponse.class);

        // Create a test subclass of OpenAIEmbeddingsClient that uses our mock HttpClient
        client = new OpenAIEmbeddingsClient(EmbeddingsModel.OI_TEXT_EMBEDDINGS_3_SMALL, validApiKey, false) {
            @Override
            protected HttpClient createHttpClient() {
                return mockHttpClient;
            }
        };

        // Setup mock response with a valid embedding response
        String mockResponseBody = createMockResponseBody();
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
    }

    private String createMockResponseBody() throws Exception {
        EmbeddingData embeddingData = new EmbeddingData("embedding", 0, List.of(0.1, 0.2, 0.3));
        EmbeddingsResponseDto responseDto = new EmbeddingsResponseDto(List.of(embeddingData), "text-embedding-3-small");
        return objectMapper.writeValueAsString(responseDto);
    }

    @Test
    void constructor_withNullApiKey_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new OpenAIEmbeddingsClient(EmbeddingsModel.OI_TEXT_EMBEDDINGS_3_SMALL, null, false);
        });

        assertEquals("apiKey cannot be null or empty", exception.getMessage());
    }

    @Test
    void constructor_withEmptyApiKey_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new OpenAIEmbeddingsClient(EmbeddingsModel.OI_TEXT_EMBEDDINGS_3_SMALL, "", false);
        });

        assertEquals("apiKey cannot be null or empty", exception.getMessage());
    }

    @Test
    void generateRequestBody_returnsCorrectMap() {
        Map<String, Object> requestBody = client.generateRequestBody("test input");

        assertEquals("text-embedding-3-small", requestBody.get("model"));
        assertEquals("test input", requestBody.get("input"));
    }

    @Test
    void generateRequest_createsHttpRequestWithCorrectHeaders() throws Exception {
        Map<String, Object> requestBody = Map.of("model", "text-embedding-3-small", "input", "test");
        HttpRequest request = client.generateRequest(requestBody);

        assertEquals(Constant.OPEN_AI_EMBEDDINGS_API_URI, request.uri());
        assertTrue(request.headers().firstValue("Authorization").isPresent());
        assertEquals("Bearer " + validApiKey, request.headers().firstValue("Authorization").get());
        assertTrue(request.headers().firstValue("Content-Type").isPresent());
        assertEquals("application/json", request.headers().firstValue("Content-Type").get());
    }

    @Test
    void getEmbeddings_sendsCorrectRequest() throws Exception {
        // Capture the HTTP request that gets sent
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        // Execute the method being tested
        client.getEmbeddings("test input");

        // Verify the request was sent with the expected parameters
        verify(mockHttpClient).send(requestCaptor.capture(), any());

        // Validate the captured request
        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals(Constant.OPEN_AI_EMBEDDINGS_API_URI, capturedRequest.uri());
        assertTrue(capturedRequest.headers().firstValue("Authorization").isPresent());
        assertEquals("Bearer " + validApiKey, capturedRequest.headers().firstValue("Authorization").get());
    }

    @Test
    void getEmbeddings_returnsCorrectResponse() throws Exception {
        // Execute the method being tested
        EmbeddingsResponseDto result = client.getEmbeddings("test input");

        // Validate the result
        assertNotNull(result);
        assertEquals("text-embedding-3-small", result.model());
        assertEquals(1, result.data().size());
        assertEquals("embedding", result.data().get(0).object());
        assertEquals(0, result.data().get(0).index());
        assertEquals(List.of(0.1, 0.2, 0.3), result.data().get(0).embedding());
    }

    @Test
    void getEmbeddings_withHttpClientError_propagatesException() throws Exception {
        // Setup mockHttpClient to throw an exception
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Verify the exception is propagated
        Exception exception = assertThrows(Exception.class, () -> {
            client.getEmbeddings("test input");
        });

        assertTrue(exception.getMessage().contains("Network error"));
    }

    @Test
    void getEmbeddings_withInvalidResponseJson_throwsException() throws Exception {
        // Setup mockResponse to return invalid JSON
        when(mockResponse.body()).thenReturn("invalid json");

        // Verify that a JSON parsing exception is thrown
        assertThrows(Exception.class, () -> {
            client.getEmbeddings("test input");
        });
    }
}