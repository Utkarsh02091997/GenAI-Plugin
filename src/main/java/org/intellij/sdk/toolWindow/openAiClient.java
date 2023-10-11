package org.intellij.sdk.toolWindow;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class openAiClient {

    private final String endpoint = "https://api.openai.com/v1/chat/completions";
    private final String apiKey = "qwertyuiop";
    private final int temperature = 0;
    private final int topP = 1;
    private final int max_token = 2048;
    private static String model = "gpt-4-32k";
    private static final ObjectMapper mapper = new ObjectMapper();

    public String getPromptOrTag(String SystemMessage, String InputTextFromUser) throws Exception {

        JSONObject jsonItem = new JSONObject();
        jsonItem.put("model", model);
        jsonItem.put("temperature", temperature);
        jsonItem.put("max_tokens", max_token);
        jsonItem.put("top_p", topP);
        JSONObject systemMsg = new JSONObject();
        JSONArray messages = new JSONArray();
        systemMsg.put("role", "system");
        systemMsg.put("content", SystemMessage);
        messages.put(systemMsg);
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", InputTextFromUser);
        messages.put(userMsg);
        jsonItem.put("messages", messages);
        String requestBody = jsonItem.toString();

        RequestEntity<String> requestEntity;
        requestEntity = RequestEntity
                .post(new URI(endpoint))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(requestBody);

        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        // Read the response body as a string
        String responseBody = responseEntity.getBody();
        String result = parseGptResponse(responseBody);

        // Handle the response as per your requirements
        return result;


    }

    private String parseGptResponse(String responseBody) throws Exception {
        if (responseBody == null || responseBody.trim().isBlank()) {
            throw new Exception("Exception in getting response");
        }
        try (JsonParser parser = mapper.getFactory().createParser(responseBody)) {
            JsonNode rootNode = parser.readValueAsTree();
            JsonNode choicesNode = rootNode.get("choices");

            if (!rootNode.has("choices")) {
                throw new Exception("Response is missing 'choices' field: " + responseBody);
            }

            if (choicesNode.size() < 1) {
                throw new Exception("Response is missing 'choices' array: " + responseBody);
            }

            String tagsOrPrompt = choicesNode.get(0).get("message").get("content").asText();

            if (tagsOrPrompt.startsWith("Prompt:")) {
                tagsOrPrompt = tagsOrPrompt.substring("Prompt:".length()).trim();
            }

            if (tagsOrPrompt.startsWith("\"") && tagsOrPrompt.endsWith("\"")) {
                tagsOrPrompt = tagsOrPrompt.substring(1, tagsOrPrompt.length() - 1);
            }

            if (Character.isLowerCase(tagsOrPrompt.charAt(0))) {
                tagsOrPrompt = Character.toUpperCase(tagsOrPrompt.charAt(0)) + tagsOrPrompt.substring(1);
            }

            return tagsOrPrompt;
        }
    }

}
