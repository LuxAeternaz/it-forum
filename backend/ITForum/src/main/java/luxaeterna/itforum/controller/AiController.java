package luxaeterna.itforum.controller;

import jakarta.validation.Valid;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.request.AiPolishRequest;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.AiPolishService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiPolishService aiPolishService;

    public AiController(AiPolishService aiPolishService) {
        this.aiPolishService = aiPolishService;
    }

    @PostMapping("/polish")
    public Result<?> polishText(@CurrentUser Long userId, @Valid @RequestBody AiPolishRequest request) {
        return Result.success(aiPolishService.polish(request.getContent(), request.getInstruction()));
    }

    @GetMapping("/agent/status")
    public Result<?> agentStatus() {
        return Result.success(true);
    }
}
