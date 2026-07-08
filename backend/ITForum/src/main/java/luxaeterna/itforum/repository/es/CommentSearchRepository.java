package luxaeterna.itforum.repository.es;

import luxaeterna.itforum.entity.es.CommentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CommentSearchRepository extends ElasticsearchRepository<CommentDocument, Long> {
}
