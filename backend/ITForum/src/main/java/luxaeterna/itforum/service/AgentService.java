package luxaeterna.itforum.service;

import luxaeterna.itforum.entity.Agent;

import java.util.List;

public interface AgentService {
    Agent createAgent(String name, String persona, String description, String avatarUrl, Long creatorUserId);
    Agent updateAgent(Long id, String name, String persona, String description, String avatarUrl, Long userId);
    void deleteAgent(Long id, Long userId);
    Agent getAgent(Long id);
    Agent findByName(String name);
    List<Agent> listAllActive();
    List<Agent> listMyAgents(Long userId);
}
