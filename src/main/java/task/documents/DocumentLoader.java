package task;

import task.clients.OpenAIEmbeddingsClient;
import task.dto.embeddings.EmbeddingsModel;
import task.utils.Constant;

import java.net.URL;

import static task.utils.Constant.DB_PASSWORD;
import static task.utils.Constant.DB_URL;
import static task.utils.Constant.DB_USER;

public class DocumentLoader {

    public static void main(String[] args) {
        try {
            OpenAIEmbeddingsClient embeddingsClient = new OpenAIEmbeddingsClient(
                    EmbeddingsModel.TEXT_EMBEDDINGS_3_SMALL,
                    Constant.API_KEY
            );

            TextProcessor processor = new TextProcessor(
                    embeddingsClient,
                    DB_URL,
                    DB_USER,
                    DB_PASSWORD
            );

            ClassLoader classLoader = DocumentLoader.class.getClassLoader();
            URL resource = classLoader.getResource("files/microwave_manual.txt");
            String path = resource.getPath();
            processor.processTextFile(path, "microwave manual", 300, 25);

            System.out.println("Document loading complete!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}