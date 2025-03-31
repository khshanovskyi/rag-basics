package task;

import task.dto.Message;

import java.util.List;
import java.util.Map;
import task.dto.Conversation;
import task.dto.Model;
import task.dto.Role;
import task.utils.Constant;

import java.util.Scanner;

public class ChatApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        OpenAIClient client = new OpenAIClient(
                Model.GPT_4o,
                Constant.API_KEY,
                List.of(
                        generateMathToolDescription(),
                        generateNasaToolStealerDescription(),
                        haikuQueryToolDescription(),
                        searchToolDescription()
                )
        );

        Conversation conversation = new Conversation();
        conversation.addMessage(new Message(Role.SYSTEM, Constant.DEFAULT_SYSTEM_PROMPT));


        System.out.println("Type your question or 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Exiting the chat. Goodbye!");
                break;
            }

            conversation.addMessage(new Message(Role.USER, userInput));

            try {
                Message aiMessage = client.responseWithMessage(conversation.getMessages());
                conversation.addMessage(aiMessage);
                System.out.println("AI: " + aiMessage.getContent());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }

    /**
     * <pre>
     * {
     *   "type": "function",
     *   "function": {
     *     "name": "simple_calculator",
     *     "description": "Provides results of the basic math calculations.",
     *     "parameters": {
     *       "type": "object",
     *       "properties": {
     *         "num1": {
     *           "type": "number",
     *           "description": "First operand."
     *         },
     *         "num2": {
     *           "type": "number",
     *           "description": "Second operand."
     *         },
     *         "operation": {
     *           "type": "string",
     *           "description": "Operation that should be performed",
     *           "enum": ["add", "subtract", "multiply", "divide"]
     *         }
     *       },
     *       "required": ["num1", "num2", "operation"],
     *       "additionalProperties": false
     *     },
     *     "strict": true
     *   }
     * }
     * </pre>
     */
    private static Map<String, Object> generateMathToolDescription() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", Constant.SIMPLE_CALCULATOR,
                        "description", "Provides results of the basic math calculations.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "num1", Map.of(
                                                "type", "number",
                                                "description", "First operand."
                                        ),
                                        "num2", Map.of(
                                                "type", "number",
                                                "description", "Second operand."
                                        ),
                                        "operation", Map.of(
                                                "type", "string",
                                                "description", "Operation that should be performed",
                                                "enum", List.of("add", "subtract", "multiply", "divide")
                                        )
                                ),
                                "required", List.of("num1", "num2", "operation"),
                                "additionalProperties", false
                        ),
                        "strict", true
                )
        );
    }

    /**
     * <pre>
     * {
     *   "type": "function",
     *   "function": {
     *     "name": "nasa_image_stealer",
     *     "description": "This tool provides description of the largest NASA image by Mars sol.",
     *     "parameters": {
     *       "type": "object",
     *       "properties": {
     *         "sol": {
     *           "type": "integer",
     *           "description": "Sol of Mars."
     *         }
     *       },
     *       "required": ["sol"],
     *       "additionalProperties": false
     *     },
     *     "strict": true
     *   }
     * }
     * </pre>
     */
    private static Map<String, Object> generateNasaToolStealerDescription() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", Constant.NASA_IMG_STEALER,
                        "description", "This tool provides description of the largest NASA image by Mars sol.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "sol", Map.of(
                                                "type", "integer",
                                                "description", "Sol of Mars."
                                        )
                                ),
                                "required", List.of("sol"),
                                "additionalProperties", false
                        ),
                        "strict", true
                )
        );
    }

    /**
     * <pre>
     * {
     *   "type": "function",
     *   "function": {
     *     "name": "nasa_image_stealer",
     *     "description": "Special tool that super experienced in Haiku generation on the Ukrainian language.",
     *     "parameters": {
     *       "type": "object",
     *       "properties": {
     *         "query": {
     *           "type": "string",
     *           "description": "Description of Haiku that should be generated"
     *         }
     *       },
     *       "required": ["query"],
     *       "additionalProperties": false
     *     },
     *     "strict": true
     *   }
     * }
     * </pre>
     */
    private static Map<String, Object> haikuQueryToolDescription() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", Constant.HAIKU_GENERATOR,
                        "description", "Special tool that super experienced in Haiku generation on the Ukrainian language.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "query", Map.of(
                                                "type", "string",
                                                "description", "Description of Haiku that should be generated"
                                        )
                                ),
                                "required", List.of("query"),
                                "additionalProperties", false
                        ),
                        "strict", true
                )
        );
    }

    /**
     * <pre>
     * {
     *   "type": "function",
     *   "function": {
     *     "name": "web_search_tool",
     *     "description": "Tool for WEB searching",
     *     "parameters": {
     *       "type": "object",
     *       "properties": {
     *         "request": {
     *           "type": "string",
     *           "description": "Search request"
     *         }
     *       },
     *       "required": ["request"],
     *       "additionalProperties": false
     *     },
     *     "strict": true
     *   }
     * }
     * </pre>
     */
    private static Map<String, Object> searchToolDescription() {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", Constant.WEB_SEARCH,
                        "description", "Tool for WEB searching.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "request", Map.of(
                                                "type", "string",
                                                "description", "Search request."
                                        )
                                ),
                                "required", List.of("request"),
                                "additionalProperties", false
                        ),
                        "strict", true
                )
        );
    }
}