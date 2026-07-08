package luxaeterna.itforum.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ModeratorAgent {

    private static final Logger log = LoggerFactory.getLogger(ModeratorAgent.class);

    private final ChatLanguageModel model;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public ModeratorAgent(ChatLanguageModel model, ObjectMapper objectMapper,
                          @Value("${ai.moderation.enabled:true}") boolean enabled) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public Map<String, Object> evaluate(String content) {
        if (!enabled) {
            return Map.of("result", "APPROVED", "reason", "Moderation disabled", "confidence", 1.0);
        }

        try {
            String prompt = PromptTemplates.buildModerationPrompt(content);
            String response = model.chat(prompt);
            response = extractJson(response);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            return result;
        } catch (Exception e) {
            log.error("Moderation evaluation failed", e);
            return Map.of("result", "APPROVED", "reason", "Evaluation error: " + e.getMessage(), "confidence", 0.0);
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
