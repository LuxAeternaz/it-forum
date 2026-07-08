package luxaeterna.itforum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${ai.openai.api-key}") String apiKey,
            @Value("${ai.openai.base-url}") String baseUrl,
            @Value("${ai.openai.model}") String model,
            @Value("${ai.openai.temperature}") double temperature,
            @Value("${ai.openai.max-tokens}") int maxTokens,
            @Value("${ai.openai.timeout}") long timeoutSeconds) {

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(long.class, ToStringSerializer.instance);
        mapper.registerModule(module);
        return mapper;
    }
}
