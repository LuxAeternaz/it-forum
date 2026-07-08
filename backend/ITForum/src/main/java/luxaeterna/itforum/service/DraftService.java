package luxaeterna.itforum.service;

import luxaeterna.itforum.dto.request.SaveDraftRequest;
import luxaeterna.itforum.entity.Draft;

public interface DraftService {
    Draft saveDraft(Long userId, SaveDraftRequest request);
    Draft getDraft(Long userId, Long postId);
    void deleteDraft(Long userId, Long postId);
}
