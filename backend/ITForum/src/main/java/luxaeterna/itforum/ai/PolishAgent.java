package luxaeterna.itforum.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PolishAgent {

    private static final Logger log = LoggerFactory.getLogger(PolishAgent.class);

    private final ChatLanguageModel model;
    private final boolean enabled;

    public PolishAgent(ChatLanguageModel model,
                       @Value("${ai.polish.enabled:true}") boolean enabled) {
        this.model = model;
        this.enabled = enabled;
    }

    public String polish(String content, String instruction) {
        if (!enabled) {
            return content;
        }

        try {
            String prompt = PromptTemplates.buildPolishPrompt(content, instruction);
            return model.chat(prompt);
        } catch (Exception e) {
            log.error("Text polishing failed", e);
            return content;
        }
    }
}
