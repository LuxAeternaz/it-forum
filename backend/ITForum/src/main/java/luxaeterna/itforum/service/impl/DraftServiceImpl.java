package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.dto.request.SaveDraftRequest;
import luxaeterna.itforum.entity.Draft;
import luxaeterna.itforum.mapper.DraftMapper;
import luxaeterna.itforum.service.DraftService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DraftServiceImpl implements DraftService {

    private final DraftMapper draftMapper;
    private final SnowflakeIdGenerator idGenerator;

    public DraftServiceImpl(DraftMapper draftMapper, SnowflakeIdGenerator idGenerator) {
        this.draftMapper = draftMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public Draft saveDraft(Long userId, SaveDraftRequest request) {
        Draft existing = draftMapper.selectByUserAndPostId(userId, request.getPostId());
        if (existing != null) {
            existing.setTitle(request.getTitle());
            existing.setContent(request.getContent());
            existing.setCategoryId(request.getCategoryId());
            draftMapper.update(existing);
            return existing;
        }
        Draft draft = new Draft();
        draft.setId(idGenerator.nextId());
        draft.setUserId(userId);
        draft.setPostId(request.getPostId());
        draft.setTitle(request.getTitle());
        draft.setContent(request.getContent());
        draft.setCategoryId(request.getCategoryId());
        draftMapper.insert(draft);
        return draft;
    }

    @Override
    public Draft getDraft(Long userId, Long postId) {
        return draftMapper.selectByUserAndPostId(userId, postId);
    }

    @Override
    @Transactional
    public void deleteDraft(Long userId, Long postId) {
        draftMapper.deleteByUserAndPostId(userId, postId);
    }
}
