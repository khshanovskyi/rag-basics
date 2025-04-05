package task.utils;

import java.net.URI;

public final class Constant {
    private Constant() {}

    public static final URI OPEN_AI_CHAT_COMPLETION_API_URI = URI.create("https://api.openai.com/v1/chat/completions");
    public static final URI OPEN_AI_EMBEDDINGS_API_URI = URI.create("https://api.openai.com/v1/embeddings");
    public static final String OPEN_AI_API_KEY = "";

    // Embeddings
    public static final String DB_URL = "jdbc:postgresql://localhost:5433/vectordb";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "postgres";
}
