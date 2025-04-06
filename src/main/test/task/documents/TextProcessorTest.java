package task.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import task.clients.OpenAIEmbeddingsClient;
import task.dto.embeddings.EmbeddingData;
import task.dto.embeddings.EmbeddingsResponseDto;
import task.documents.TextProcessor.SearchMode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TextProcessor class
 */
class TextProcessorTest {

    private OpenAIEmbeddingsClient mockEmbeddingsClient;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private TextProcessor textProcessor;

    @BeforeEach
    void setUp() {
        // Create mocks but don't configure behavior here - do it in individual tests
        mockEmbeddingsClient = Mockito.mock(OpenAIEmbeddingsClient.class);
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);

        // Create the text processor with overridden getConnection method
        textProcessor = new TextProcessor(mockEmbeddingsClient, "jdbc:mock", "user", "pass") {
            @Override
            protected Connection getConnection() {
                return mockConnection;
            }

            @Override
            protected String getContent(String fileName) throws IOException {
                if (fileName.equals("test-file.txt")) {
                    return "This is a test content for chunking. It should be divided into smaller pieces based on the chunk size and overlap parameters.";
                }
                return super.getContent(fileName);
            }
        };
    }

    @Test
    void chunkText_withShortText_returnsSingleChunk() throws Exception {
        // Use reflection to access private method
        Method chunkTextMethod = TextProcessor.class.getDeclaredMethod("chunkText", String.class, int.class, int.class);
        chunkTextMethod.setAccessible(true);

        String shortText = "Short text";
        int chunkSize = 20;
        int overlap = 5;

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) chunkTextMethod.invoke(textProcessor, shortText, chunkSize, overlap);

        assertEquals(1, result.size());
        assertEquals(shortText, result.get(0));
    }

    @Test
    void chunkText_withLongText_returnsMultipleChunks() throws Exception {
        // Use reflection to access private method
        Method chunkTextMethod = TextProcessor.class.getDeclaredMethod("chunkText", String.class, int.class, int.class);
        chunkTextMethod.setAccessible(true);

        String longText = "This is a longer text that should be split into multiple chunks based on the chunk size and overlap parameters.";
        int chunkSize = 20;
        int overlap = 5;

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) chunkTextMethod.invoke(textProcessor, longText, chunkSize, overlap);

        // Verify basic expectations
        assertTrue(result.size() > 1);

        // Don't hardcode exact expectation for chunk content - instead check pattern
        assertTrue(result.get(0).startsWith("This is a longer"));
        assertTrue(result.get(1).contains("text that"));
    }

    @Test
    void embeddingListToString_convertsCorrectly() {
        List<Double> embedding = Arrays.asList(0.1, 0.2, 0.3, 0.4);
        String result = textProcessor.embeddingListToString(embedding);

        assertEquals("[0.1,0.2,0.3,0.4]", result);
    }

    @Test
    void trunkateTable_executesCorrectSql() throws SQLException {
        // Setup mock
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Call the method
        textProcessor.trunkateTable();

        // Verify
        verify(mockConnection).prepareStatement("TRUNCATE TABLE items");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void saveChunk_insertsDataCorrectly() throws Exception {
        // Setup mock
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Test data
        List<Double> embedding = Arrays.asList(0.1, 0.2, 0.3);
        String chunk = "Test chunk";
        String documentName = "test.txt";

        // Call the method
        textProcessor.saveChunk(embedding, chunk, documentName);

        // Verify
        verify(mockConnection).prepareStatement("INSERT INTO items (document_name, text, embedding) VALUES (?, ?, ?::vector)");
        verify(mockPreparedStatement).setString(1, documentName);
        verify(mockPreparedStatement).setString(2, chunk);
        verify(mockPreparedStatement).setString(3, "[0.1,0.2,0.3]");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void processTextFile_withInvalidChunkSize_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            textProcessor.processTextFile("test-file.txt", 5, 0, false, false);
        });

        assertEquals("chunkSize must be at least 10", exception.getMessage());
    }

    @Test
    void processTextFile_withNegativeOverlap_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            textProcessor.processTextFile("test-file.txt", 20, -5, false, false);
        });

        assertEquals("overlap must be at least 0", exception.getMessage());
    }

    @Test
    void processTextFile_withOverlapLargerThanChunkSize_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            textProcessor.processTextFile("test-file.txt", 20, 25, false, false);
        });

        assertEquals("overlap should be lower than chunkSize", exception.getMessage());
    }

    @Test
    void processTextFile_processesProperly() throws Exception {
        // Setup mocks for database
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Setup mock for embedding client
        List<Double> mockEmbedding = Arrays.asList(0.1, 0.2, 0.3);
        EmbeddingData embeddingData = new EmbeddingData("embedding", 0, mockEmbedding);
        EmbeddingsResponseDto responseDto = new EmbeddingsResponseDto(List.of(embeddingData), "text-embedding-3-small");
        when(mockEmbeddingsClient.getEmbeddings(anyString())).thenReturn(responseDto);

        // Call the method
        textProcessor.processTextFile("test-file.txt", 20, 5, true, false);

        // Verify truncate was called if requested
        verify(mockConnection).prepareStatement("TRUNCATE TABLE items");

        // Verify embeddings were requested for each chunk
        verify(mockEmbeddingsClient, atLeastOnce()).getEmbeddings(anyString());

        // Verify data was saved to the database
        verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
    }

    @Test
    void similarityEuclideanSearch_basicBehaviorTest() throws Exception {
        // 1. Setup embedding response
        List<Double> mockEmbedding = Arrays.asList(0.1, 0.2, 0.3);
        EmbeddingData embeddingData = new EmbeddingData("embedding", 0, mockEmbedding);
        EmbeddingsResponseDto responseDto = new EmbeddingsResponseDto(List.of(embeddingData), "text-embedding-3-small");
        when(mockEmbeddingsClient.getEmbeddings(anyString())).thenReturn(responseDto);

        // 2. Setup database connection and statement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // 3. Setup result set with some sample results
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("text")).thenReturn("Sample Text 1", "Sample Text 2");
        when(mockResultSet.getString("similarity_score")).thenReturn("0.95", "0.85");

        // 4. Call the method
        List<String> results = textProcessor.similarityEuclideanSearch("any query", 5, 0.5f);

        // 5. Only verify that we got the expected number of results
        assertEquals(2, results.size());

        // 6. Verify correct query used (Euclidean distance)
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("embedding <-> ?::vector"));

        // 7. Verify parameters were set correctly
        verify(mockPreparedStatement).setString(1, "[0.1,0.2,0.3]");  // Vector
        verify(mockPreparedStatement).setString(2, "[0.1,0.2,0.3]");  // Vector again
        verify(mockPreparedStatement).setFloat(3, 0.5f);  // Minimum score
        verify(mockPreparedStatement).setInt(4, 5);  // Top K
    }

    @Test
    void similarityCosineSearch_basicBehaviorTest() throws Exception {
        // 1. Setup embedding response
        List<Double> mockEmbedding = Arrays.asList(0.1, 0.2, 0.3);
        EmbeddingData embeddingData = new EmbeddingData("embedding", 0, mockEmbedding);
        EmbeddingsResponseDto responseDto = new EmbeddingsResponseDto(List.of(embeddingData), "text-embedding-3-small");
        when(mockEmbeddingsClient.getEmbeddings(anyString())).thenReturn(responseDto);

        // 2. Setup database connection and statement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // 3. Setup result set with some sample results
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString("text")).thenReturn("Sample Text A", "Sample Text B", "Sample Text C");
        when(mockResultSet.getString("similarity_score")).thenReturn("0.95", "0.85", "0.75");

        // 4. Call the method
        List<String> results = textProcessor.similarityCosineSearch("any query", 5, 0.5f);

        // 5. Only verify that we got the expected number of results
        assertEquals(3, results.size());

        // 6. Verify correct query used (Cosine distance)
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("embedding <=> ?::vector"));

        // 7. Verify parameters were set correctly
        verify(mockPreparedStatement).setString(1, "[0.1,0.2,0.3]");  // Vector
        verify(mockPreparedStatement).setString(2, "[0.1,0.2,0.3]");  // Vector again
        verify(mockPreparedStatement).setFloat(3, 0.5f);  // Minimum score
        verify(mockPreparedStatement).setInt(4, 5);  // Top K
    }

    @Test
    void search_withInvalidTopK_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            textProcessor.similarityEuclideanSearch("query", 0, 0.8f);
        });

        assertEquals("topK must be at least 1", exception.getMessage());
    }

    @Test
    void search_withInvalidMinScore_throwsException() {
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            textProcessor.similarityEuclideanSearch("query", 3, -0.1f);
        });

        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            textProcessor.similarityEuclideanSearch("query", 3, 1.1f);
        });

        assertEquals("minScore must be in [0.0..., 0.99...] diapasons", exception1.getMessage());
        assertEquals("minScore must be in [0.0..., 0.99...] diapasons", exception2.getMessage());
    }

    @Test
    void generateSearchQuery_forSimilarityMode_returnsCorrectQuery() {
        String query = textProcessor.generateSearchQuery(SearchMode.SIMILARITY);
        assertTrue(query.contains("embedding <-> ?::vector"));
    }

    @Test
    void generateSearchQuery_forSemanticMode_returnsCorrectQuery() {
        String query = textProcessor.generateSearchQuery(SearchMode.SEMANTIC);
        assertTrue(query.contains("embedding <=> ?::vector"));
    }

    @Test
    void process_withValidInput_callsEmbeddingsClientAndSavesChunk() throws Exception {
        // 1. Setup for embedding client
        List<Double> mockEmbedding = Arrays.asList(0.1, 0.2, 0.3);
        EmbeddingData embeddingData = new EmbeddingData("embedding", 0, mockEmbedding);
        EmbeddingsResponseDto responseDto = new EmbeddingsResponseDto(List.of(embeddingData), "text-embedding-3-small");
        when(mockEmbeddingsClient.getEmbeddings("test chunk")).thenReturn(responseDto);

        // 2. Setup for database
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // 3. Call the method
        textProcessor.process("test.txt", "test chunk");

        // 4. Verify embeddings were generated
        verify(mockEmbeddingsClient).getEmbeddings("test chunk");

        // 5. Verify data was saved to DB
        verify(mockConnection).prepareStatement("INSERT INTO items (document_name, text, embedding) VALUES (?, ?, ?::vector)");
        verify(mockPreparedStatement).setString(1, "test.txt");
        verify(mockPreparedStatement).setString(2, "test chunk");
        verify(mockPreparedStatement).setString(3, "[0.1,0.2,0.3]");
        verify(mockPreparedStatement).executeUpdate();
    }
}