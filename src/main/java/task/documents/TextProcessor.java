package task;

import task.clients.OpenAIEmbeddingsClient;
import task.dto.embeddings.EmbeddingsResponseDto;

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

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public void processTextFile(String filePath, String documentName, int chunkSize, int overlap) throws Exception {
        if (chunkSize < 10) throw new IllegalArgumentException("chunkSize must be at least 10");
        if (overlap < 0) throw new IllegalArgumentException("overlap must be at least 0");
        if (overlap >= chunkSize) throw new IllegalArgumentException("overlap should be lower than chunkSize");

        trunkateTable();

        String content = Files.readString(Path.of(filePath));
        List<String> chunks = chunkText(content, chunkSize, overlap);

        System.out.println("Processing document: " + documentName);
        System.out.println("Total chunks: " + chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            embedAndStoreChunk(chunk, documentName, i);
        }
    }

    public List<String> chunkText(String text, int chunkSize, int overlap) {
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

    private void embedAndStoreChunk(String chunk, String documentName, int chunkIndex) throws Exception {
        EmbeddingsResponseDto embeddings = embeddingsClient.getEmbeddings(chunk);
        List<Double> embeddingVector = embeddings.data().getFirst().embedding();

        String vectorString = embeddingListToString(embeddingVector);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO items (name, description, embedding) VALUES (?, ?, ?::vector)")) {

            ps.setString(1, documentName + " - Chunk " + chunkIndex);
            ps.setString(2, chunk);
            ps.setString(3, vectorString);
            ps.executeUpdate();
        }

        System.out.println("Stored chunk " + chunkIndex + " for document: " + documentName);
    }

    private String embeddingListToString(List<Double> embedding) {
        String embeddings = embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return String.format("[%s]", embeddings);
    }

    private void trunkateTable() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "TRUNCATE TABLE items")) {

            ps.executeUpdate();
        }
    }


    public List<String> retrieveSimilarChunks(String query, int topK) throws Exception {
        EmbeddingsResponseDto queryEmbedding = embeddingsClient.getEmbeddings(query);
        List<Double> embeddingVector = queryEmbedding.data().getFirst().embedding();
        String vectorString = embeddingListToString(embeddingVector);

        List<String> retrievedChunks = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT description FROM items ORDER BY embedding <-> ?::vector LIMIT ?")) {

            ps.setString(1, vectorString);
            ps.setInt(2, topK);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    retrievedChunks.add(rs.getString("description"));
                }
            }
        }

        return retrievedChunks;
    }
}