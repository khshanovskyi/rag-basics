package task.documents;

import task.clients.OpenAIEmbeddingsClient;
import task.dto.embeddings.EmbeddingsResponseDto;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A processor for text documents that handles chunking, embedding, storing, and retrieval.
 *
 * @see OpenAIEmbeddingsClient
 * @see EmbeddingsResponseDto
 */
public class TextProcessor {

    private final OpenAIEmbeddingsClient embeddingsClient;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public TextProcessor(OpenAIEmbeddingsClient embeddingsClient,
                         String dbUrl,
                         String dbUser,
                         String dbPassword) {
        this.embeddingsClient = embeddingsClient;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Loads content from file, slices it into chunks, generates embeddings, and persists chunk content with embeddings to DB.
     *
     * @param fileName file name with path that should be converted to embeddings and persisted in DB
     * @param chunkSize chars amount that we need to slice content from file
     * @param overlap overlap chars amount
     * @param truncateTable whether we need to clean DB table or not
     * @param runParallel whether save chunks in parallel or not
     */
    public void processTextFile(String fileName, int chunkSize, int overlap, boolean truncateTable, boolean runParallel) throws Exception {
        if (chunkSize < 10) throw new IllegalArgumentException("chunkSize must be at least 10");
        if (overlap < 0) throw new IllegalArgumentException("overlap must be at least 0");
        if (overlap >= chunkSize) throw new IllegalArgumentException("overlap should be lower than chunkSize");

        String content = getContent(fileName);
        List<String> chunks = chunkText(content, chunkSize, overlap);

        System.out.println("Processing document: " + fileName);
        System.out.println("Total chunks: " + chunks.size());

        Stream<String> chunksStream = runParallel ? chunks.parallelStream() : chunks.stream();

        if (truncateTable){
            trunkateTable();
        }

        chunksStream.forEach(chunk -> {
            process(fileName, chunk);
        });
    }

    protected String getContent(String fileName) throws IOException {
        ClassLoader classLoader = TextProcessor.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        String filePath = resource.getPath();
        return Files.readString(Path.of(filePath));
    }

    protected void process(String documentName, String chunk) {
        try {
            EmbeddingsResponseDto embeddings = embeddingsClient.getEmbeddings(chunk);
            List<Double> embeddingVector = embeddings.data().getFirst().embedding();

            saveChunk(embeddingVector, chunk, documentName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void saveChunk(List<Double> embeddingVector, String chunk, String documentName) throws Exception {
        String vectorString = embeddingListToString(embeddingVector);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO items (document_name, text, embedding) VALUES (?, ?, ?::vector)")) {

            ps.setString(1, documentName);
            ps.setString(2, chunk);
            ps.setString(3, vectorString);
            ps.executeUpdate();
        }

        System.out.printf("Stored chunk '%s' from document: %s\n%n", chunk, documentName);
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int currentPosition = 0;
        while (currentPosition < text.length()) {
            int endPosition = Math.min(currentPosition + chunkSize, text.length());

            String chunk = text.substring(currentPosition, endPosition);
            chunks.add(chunk);

            currentPosition = endPosition - overlap;

            if (currentPosition >= text.length() - overlap && endPosition == text.length()) {
                break;
            }
        }

        return chunks;
    }

    protected void trunkateTable() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "TRUNCATE TABLE items")) {

            ps.executeUpdate();
            System.out.println("Table has been successfully truncated.");
        }
    }

    protected String embeddingListToString(List<Double> embedding) {
        String embeddings = embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return String.format("[%s]", embeddings);
    }


    /**
     * Makes similarity search of data chunks by user input with Euclidean distance.
     *
     * @param userRequest original user input
     * @param topK amount of results to return after search
     * @param minScore [0f - 0.99999999f], filter with similarity score
     * @return chunks of most suitable data to user input
     */
    public List<String> similarityEuclideanSearch(String userRequest, int topK, float minScore) throws Exception {
        return search(SearchMode.SIMILARITY_E, userRequest, topK, minScore);
    }

    /**
     * Makes similarity search of data chunks by user input with Cosine distance.
     *
     * @param userRequest original user input
     * @param topK amount of results to return after search
     * @param minScore [0f - 0.99999999f], filter with similarity score
     * @return chunks of most suitable data to user input
     */
    public List<String> similarityCosineSearch(String userRequest, int topK, float minScore) throws Exception {
        return search(SearchMode.SIMILARITY_C, userRequest, topK, minScore);
    }

    protected List<String> search(SearchMode searchMode, String userRequest, int topK, float minScore) throws Exception {
        if (topK < 1) throw new IllegalArgumentException("topK must be at least 1");
        if (minScore < 0 || minScore > 1) throw new IllegalArgumentException("minScore must be in [0.0..., 0.99...] diapasons");

        EmbeddingsResponseDto queryEmbedding = embeddingsClient.getEmbeddings(userRequest);
        List<Double> embeddingVector = queryEmbedding.data().getFirst().embedding();
        String vectorString = embeddingListToString(embeddingVector);

        List<String> retrievedChunks = new ArrayList<>();

        String searchQuery = generateSearchQuery(searchMode);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(searchQuery)) {

            ps.setString(1, vectorString);
            ps.setString(2, vectorString);
            ps.setFloat(3, minScore);
            ps.setInt(4, topK);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString("text") + " " + rs.getString("similarity_score"));
                    retrievedChunks.add(rs.getString("text"));
                }
                System.out.println();
            }
        }

        return retrievedChunks;
    }

    /*
    For L2 (Euclidean) distance (`<->`):
        - The L2 distance measures the straight-line distance between two points in the vector space.
        - For normalized embeddings (vectors with a length of 1), the maximum possible L2 distance between any two vectors is 2.
        - A distance of 0 means the vectors are identical (perfect match).
        - A distance of 2 means the vectors are pointing in exactly opposite directions (worst match).

    For cosine distance (`<=>`):
        - The cosine distance measures the angular difference between vectors.
        - For normalized embeddings, the cosine distance also ranges from 0 to 2.
        - A distance of 0 means the vectors are pointing in the same direction (perfect match).
        - A distance of 2 means the vectors are pointing in exactly opposite directions (worst match).

    In your query, you're transforming these distance metrics into similarity scores with this formula: similarity_score = 1 - (distance) / 2

    This transformation does two important things:
        1. It divides the distance by 2, mapping the range from [0,2] to [0,1]
        2. It subtracts this value from 1, which inverts the scale

    As a result:
        - A distance of 0 (identical vectors) becomes a similarity score of 1 (100% similar)
        - A distance of 2 (completely opposite vectors) becomes a similarity score of 0 (0% similar)
    */
    protected String generateSearchQuery(SearchMode searchMode) {
        return switch (searchMode) {
            case SIMILARITY_E ->
                    """
                    SELECT text, 1 - (embedding <-> ?::vector) / 2 AS similarity_score
                    FROM items
                    WHERE 1 - (embedding <-> ?::vector) / 2 >= ?
                    ORDER BY similarity_score DESC LIMIT ?
                    """;
            case SIMILARITY_C ->
                    """
                    SELECT text, 1 - (embedding <=> ?::vector) / 2 AS similarity_score
                    FROM items
                    WHERE 1 - (embedding <=> ?::vector) / 2 >= ?
                    ORDER BY similarity_score DESC LIMIT ?
                    """;
        };
    }

    protected enum SearchMode {
        SIMILARITY_E, // Euclidean distance (<->)
        SIMILARITY_C, // Cosine distance (<=>)
    }

}