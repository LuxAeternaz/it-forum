package luxaeterna.itforum.controller;

import jakarta.validation.Valid;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.request.CreateAgentRequest;
import luxaeterna.itforum.dto.request.UpdateAgentRequest;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.AgentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public Result<?> listAllActive() {
        return Result.success(agentService.listAllActive());
    }

    @GetMapping("/my")
    public Result<?> listMyAgents(@CurrentUser Long userId) {
        return Result.success(agentService.listMyAgents(userId));
    }

    @GetMapping("/{id}")
    public Result<?> getAgent(@PathVariable Long id) {
        return Result.success(agentService.getAgent(id));
    }

    @PostMapping
    public Result<?> createAgent(@CurrentUser Long userId, @Valid @RequestBody CreateAgentRequest request) {
        return Result.success(agentService.createAgent(request.getName(), request.getPersona(),
                request.getDescription(), request.getAvatarUrl(), userId));
    }

    @PutMapping("/{id}")
    public Result<?> updateAgent(@CurrentUser Long userId, @PathVariable Long id,
                                  @Valid @RequestBody UpdateAgentRequest request) {
        return Result.success(agentService.updateAgent(id, request.getName(), request.getPersona(),
                request.getDescription(), request.getAvatarUrl(), userId));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAgent(@CurrentUser Long userId, @PathVariable Long id) {
        agentService.deleteAgent(id, userId);
        return Result.success();
    }
}
