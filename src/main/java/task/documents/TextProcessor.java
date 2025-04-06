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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        //todo: 1. Validate parameters (chunkSize >= 10, overlap >= 0, overlap < chunkSize), otherwise IllegalArgumentException
        //todo: 2. Read the text content from the file using `getContent(fileName)`. (Implemented)
        //todo: 3. Split the content into chunks using `chunkText(content, chunkSize, overlap)`. (Implemented)
        //todo: 4. Log information about processing (document name, total chunks)
        //todo: 5. Create a stream of chunks (parallel if runParallel is true)
        //todo: 6. If truncateTable is true, call `truncateTable()` to clear the table. (Implemented)
        //todo: 7. Process each chunk by calling `process(fileName, chunk)`. (Need to implement*)

        throw new RuntimeException("Not implemented");
    }

    protected String getContent(String fileName) throws IOException {
        ClassLoader classLoader = TextProcessor.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        String filePath = resource.getPath();
        return Files.readString(Path.of(filePath));
    }

    protected void process(String documentName, String chunk) {
        //todo: 1. Get embeddings for the chunk using `embeddingsClient.getEmbeddings(chunk)`
        //todo: 2. Extract the embedding vector from the response
        //todo: 3. Save the chunk and its embeddings to the database using `saveChunk(embeddingVector, chunk, documentName)`. (Need to implement*)
        //todo: 4. Handle any exceptions by wrapping them in RuntimeException

        throw new RuntimeException("Not implemented");
    }

    protected void saveChunk(List<Double> embeddingVector, String chunk, String documentName) throws Exception {
        //todo: 1. Convert the embedding vector list to a string using `embeddingListToString(embeddingVector)`. (Implemented)
        //todo: 2. Create a database connection
        //todo: 3. Prepare an SQL statement to insert document_name, text, and embedding into items table
        //todo: 4. Set parameters for the prepared statement (documentName, chunk, vectorString)
        //todo: 5. Execute the update
        //todo: 6. Print information about the stored chunk
        //todo: 7. Close the connection and statement properly

        throw new RuntimeException("Not implemented");
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
     * Makes similarity search of data chunks by user input.
     *
     * @param userRequest original user input
     * @param topK amount of results to return after search
     * @param minScore [0f - 0.99999999f], filter with similarity score
     * @return chunks of most suitable data to user input
     */
    public List<String> similarityEuclideanSearch(String userRequest, int topK, float minScore) throws Exception {
        //todo: 1. Call the search method with SearchMode.SIMILARITY parameter
        //todo: 2. Return the result of the method `search`. (Need to implement*)

        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes semantic search of data chunks by user input.
     *
     * @param userRequest original user input
     * @param topK amount of results to return after search
     * @param minScore [0f - 0.99999999f], filter with similarity score
     * @return chunks of most suitable data to user input
     */
    public List<String> similarityCosineSearch(String userRequest, int topK, float minScore) throws Exception {
        //todo: 1. Call the search method with SearchMode.SEMANTIC parameter
        //todo: 2. Return the result of the method `search`. (Need to implement*)

        throw new RuntimeException("Not implemented");
    }

    protected List<String> search(SearchMode searchMode, String userRequest, int topK, float minScore) throws Exception {
        //todo: 1. Validate parameters (topK >= 1, minScore between 0 and 1), otherwise IllegalArgumentException
        //todo: 2. Get embeddings for the user request using `embeddingsClient.getEmbeddings(userRequest)`
        //todo: 3. Extract the embedding vector from the response
        //todo: 4. Convert the embedding vector to string format using `embeddingListToString(embeddingVector)`. (Implemented)
        //todo: 5. Initialize a list to store retrieved chunks (results)
        //todo: 6. Generate the appropriate search query based on searchMode using `generateSearchQuery(searchMode)`. (Need to implement*)
        //todo: 7. Create a database connection and prepare the SQL statement
        //todo: 8. Set parameters for the statement (vectorString, vectorString, minScore, topK)
        //todo: 9. Execute the query and process the results, adding text to retrievedChunks
        //todo: 10. Log information about the retrieved chunks (optional)
        //todo: 11. Close the connection and statement properly
        //todo: 12. Return the list of retrieved chunks

        throw new RuntimeException("Not implemented");
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
        //todo: 1. Implement a switch statement based on the searchMode parameter
        //todo: 2. For SearchMode.SIMILARITY_E, return SQL query that:
        //todo:    - Selects `tex`t and calculates `similarity_score` using the L2 distance operator (<->)
        //todo:    - Transforms distance to similarity with formula: 1 - (distance) / 2
        //todo:      (Division by 2 normalizes the score to 0-1 range since L2 distances can be up to 2 for normalized vectors)
        //todo:    - Filters by minimum similarity score threshold
        //todo:    - Orders results by similarity score in descending order
        //todo:    - Limits to the specified number of results
        //todo: 3. For SearchMode.SIMILARITY_C, return SQL query that:
        //todo:    - All the same as query above but use the cosine distance operator (<=>)
        //todo: 4. Include necessary vector casting (?::vector) in the queries

        throw new RuntimeException("Not implemented");
    }

    protected enum SearchMode {
        SIMILARITY_E, // Euclidean distance (<->)
        SIMILARITY_C, // Cosine distance (<=>)
    }

}