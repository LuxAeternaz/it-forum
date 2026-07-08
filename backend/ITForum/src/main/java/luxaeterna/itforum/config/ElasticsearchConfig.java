package luxaeterna.itforum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "luxaeterna.itforum.repository.es")
public class ElasticsearchConfig {
}
