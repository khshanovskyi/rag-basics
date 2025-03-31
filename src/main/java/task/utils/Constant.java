package task.utils;

import java.net.URI;

public final class Constant {
    private Constant() {}

    public static final String DEFAULT_SYSTEM_PROMPT = "You are an advanced AI agent. Your goal is to assist user with his questions.";
    public static final URI OPEN_AI_API_URI = URI.create("https://api.openai.com/v1/chat/completions");
    public static final String API_KEY = "";


    public final static String NASA_API_KEY = "";
    public final static String NASA_API = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";


    public final static String SIMPLE_CALCULATOR = "simple_calculator";
    public final static String NASA_IMG_STEALER = "nasa_image_stealer";
    public final static String HAIKU_GENERATOR = "haiku_generation_tool";
    public final static String WEB_SEARCH = "web_search_tool";
}
