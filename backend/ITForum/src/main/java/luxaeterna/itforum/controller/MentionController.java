package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.response.MentionSuggestion;
import luxaeterna.itforum.entity.Agent;
import luxaeterna.itforum.mapper.AgentMapper;
import luxaeterna.itforum.mapper.FollowMapper;
import luxaeterna.itforum.security.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mentions")
public class MentionController {

    private final AgentMapper agentMapper;
    private final FollowMapper followMapper;

    public MentionController(AgentMapper agentMapper, FollowMapper followMapper) {
        this.agentMapper = agentMapper;
        this.followMapper = followMapper;
    }

    @GetMapping("/suggest")
    public Result<List<MentionSuggestion>> suggest(
            @CurrentUser Long userId,
            @RequestParam String q,
            @RequestParam(defaultValue = "8") int limit) {

        List<MentionSuggestion> suggestions = new ArrayList<>();
        String keyword = q.trim();
        if (keyword.isEmpty()) {
            return Result.success(suggestions);
        }

        // Search agents
        List<Agent> agents = agentMapper.searchByName(keyword, limit);
        for (Agent agent : agents) {
            suggestions.add(new MentionSuggestion(
                    String.valueOf(agent.getId()),
                    agent.getName(),
                    agent.getAvatarUrl(),
                    "agent"
            ));
        }

        // Search mutual follows (only if userId is available)
        if (userId != null) {
            List<Map<String, Object>> mutuals = followMapper.selectMutualFollows(userId);
            for (Map<String, Object> user : mutuals) {
                String username = (String) user.get("username");
                if (username != null && username.toLowerCase().contains(keyword.toLowerCase())) {
                    if (suggestions.size() >= limit * 2) break;
                    String id = String.valueOf(user.get("id"));
                    // Skip duplicates
                    boolean duplicate = suggestions.stream().anyMatch(s -> s.getId().equals(id) && "user".equals(s.getType()));
                    if (!duplicate) {
                        suggestions.add(new MentionSuggestion(
                                id,
                                username,
                                (String) user.get("avatarUrl"),
                                "user"
                        ));
                    }
                }
            }
        }

        return Result.success(suggestions);
    }
}
