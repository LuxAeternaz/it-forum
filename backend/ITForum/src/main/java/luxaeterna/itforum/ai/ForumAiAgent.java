package luxaeterna.itforum.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ForumAiAgent {

    private static final Logger log = LoggerFactory.getLogger(ForumAiAgent.class);

    private final ChatLanguageModel model;
    private final boolean enabled;

    public ForumAiAgent(ChatLanguageModel model,
                        @Value("${ai.agent.enabled:true}") boolean enabled) {
        this.model = model;
        this.enabled = enabled;
    }

    public String respond(String postContext, String threadContext, String userPrompt) {
        return respond("IT小助手", "友好、耐心、对技术充满热情的技术论坛AI助手。技术回答专业准确，不确定时会坦诚说明。主要用中文回答。", postContext, threadContext, userPrompt);
    }

    public String respond(String agentName, String agentPersona, String postContext, String threadContext, String userPrompt) {
        if (!enabled) {
            return "AI助手当前未启用，请稍后再试。";
        }

        try {
            String prompt = PromptTemplates.buildAgentPrompt(agentName, agentPersona, postContext, threadContext, userPrompt);
            return model.chat(prompt);
        } catch (Exception e) {
            log.error("AI agent response failed", e);
            return "抱歉，我暂时无法回答这个问题，请稍后再试。";
        }
    }
}
