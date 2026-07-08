package luxaeterna.itforum.repository.es;

import luxaeterna.itforum.entity.es.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
}
