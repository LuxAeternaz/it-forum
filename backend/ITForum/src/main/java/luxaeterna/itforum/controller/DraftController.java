package luxaeterna.itforum.controller;

import jakarta.validation.Valid;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.request.SaveDraftRequest;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.DraftService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @GetMapping
    public Result<?> getDraft(@CurrentUser Long userId, @RequestParam(required = false) Long postId) {
        return Result.success(draftService.getDraft(userId, postId));
    }

    @PutMapping
    public Result<?> saveDraft(@CurrentUser Long userId, @Valid @RequestBody SaveDraftRequest request) {
        return Result.success(draftService.saveDraft(userId, request));
    }

    @DeleteMapping
    public Result<?> deleteDraft(@CurrentUser Long userId, @RequestParam(required = false) Long postId) {
        draftService.deleteDraft(userId, postId);
        return Result.success();
    }
}
