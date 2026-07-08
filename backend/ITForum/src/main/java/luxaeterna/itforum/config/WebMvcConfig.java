package luxaeterna.itforum.config;

import luxaeterna.itforum.security.CurrentUserMethodArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    private final CurrentUserMethodArgumentResolver currentUserResolver;
    private final String uploadPath;

    public WebMvcConfig(CurrentUserMethodArgumentResolver currentUserResolver,
                        @Value("${file.upload.path}") String uploadPath) {
        this.currentUserResolver = currentUserResolver;
        this.uploadPath = uploadPath;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (int i = 0; i < converters.size(); i++) {
            HttpMessageConverter<?> converter = converters.get(i);

            if (converter instanceof JacksonJsonHttpMessageConverter jacksonConverter) {
                // Spring 7.x: tools.jackson (Eclipse Jackson 3.x)
                JsonMapper mapper = jacksonConverter.getMapper();
                SimpleModule module = new SimpleModule();
                module.addSerializer(Long.class, ToStringSerializer.instance);
                module.addSerializer(long.class, ToStringSerializer.instance);
                JsonMapper newMapper = mapper.rebuild().addModule(module).build();
                converters.set(i, new JacksonJsonHttpMessageConverter(newMapper));
                log.info("Registered Long→String serializer on tools.jackson JsonMapper");
            } else if (converter instanceof AbstractJackson2HttpMessageConverter legacyConverter) {
                // Legacy: com.fasterxml.jackson (Classic Jackson 2.x)
                com.fasterxml.jackson.databind.module.SimpleModule module =
                    new com.fasterxml.jackson.databind.module.SimpleModule();
                module.addSerializer(Long.class,
                    com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance);
                module.addSerializer(long.class,
                    com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance);
                legacyConverter.getObjectMapper().registerModule(module);
                log.info("Registered Long→String serializer on com.fasterxml.jackson ObjectMapper");
            }
        }
    }
}
