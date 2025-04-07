package task;

import task.clients.OpenAIChatCompletionClient;
import task.clients.OpenAIEmbeddingsClient;
import task.documents.TextProcessor;
import task.dto.chat_completion.Conversation;
import task.dto.chat_completion.Message;
import task.dto.chat_completion.ChatCompletionModel;
import task.dto.chat_completion.Role;
import task.dto.embeddings.EmbeddingsModel;
import task.utils.Constant;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static task.utils.Constant.DB_PASSWORD;
import static task.utils.Constant.DB_URL;
import static task.utils.Constant.DB_USER;

public class ChatApp {

    private static final String SYSTEM_PROMPT = """
            You are a RAG-powered assistant that assists users with their questions about microwave usage.
            
            ## Structure of User message:
            **USER QUESTION** - The user's actual question.
            **RAG CONTEXT** - Retrieved documents relevant to the query.
            
            ## Instructions:
            - Use information from **RAG CONTEXT** as context when answering the **USER QUESTION**.
            - Cite specific sources when using information from the context.
            - Answer ONLY based on conversation history and RAG context.
            - If no relevant information exists in **RAG CONTEXT** or conversation history, state that you cannot answer the question.
            """;

    private static final String USER_PROMPT = "**USER QUESTION**: %s \n\n **RAG CONTEXT**: %s";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // todo: initiate it with:
        //  - gpt4o ChatCompletionModel
        //  - api key from Constant
        //  - `printRequestResponse` is up to you
        OpenAIChatCompletionClient chatCompletionClient = null;

        // todo: initiate it with:
        //  - SMALL EmbeddingsModel
        //  - api key from Constant
        //  - `printResponse` is up to you
        OpenAIEmbeddingsClient embeddingsClient = null;

        TextProcessor textProcessor = new TextProcessor(
                embeddingsClient,
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );

        System.out.println("Add new data and embeddings to the database? (y/n)");
        String addNewEmbeddings = scanner.nextLine();
        if (addNewEmbeddings.equalsIgnoreCase("y") || addNewEmbeddings.equalsIgnoreCase("yes")) {
            textProcessor.processTextFile(
                    "files/microwave_manual.txt",
                    200,
                    25,
                    true,
                    true
            );
            System.out.println("----------\n New data and embeddings successfully added to the database!\n");
        }


        Conversation conversation = new Conversation();
        conversation.addMessage(new Message(Role.SYSTEM, SYSTEM_PROMPT));

        System.out.println("Type your question or 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Exiting the chat. Goodbye!");
                break;
            }

            try {
                // Search the most relevant context to user request in Vector DB
                // TODO: use text processor for search:
                // TODO: - play with `topK` to check the amount of returned results
                // TODO: - play with `minScore` to check the amount of returned results and their `similarity_score`
                List<String> ragContextChunks = null;

                userInput = String.format(
                        USER_PROMPT,
                        userInput,
                        String.join("\n\n", Objects.requireNonNull(ragContextChunks, "ragContextChunks cannot be null"))
                );


                conversation.addMessage(new Message(Role.USER, userInput));

                Message aiMessage = chatCompletionClient.responseWithMessage(conversation.getMessages());
                conversation.addMessage(aiMessage);
                System.out.println("AI: " + aiMessage.getContent());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }

}