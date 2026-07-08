package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.entity.Agent;
import luxaeterna.itforum.exception.BusinessException;
import luxaeterna.itforum.exception.ResourceNotFoundException;
import luxaeterna.itforum.mapper.AgentMapper;
import luxaeterna.itforum.service.AgentService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    private final AgentMapper agentMapper;
    private final SnowflakeIdGenerator idGenerator;

    public AgentServiceImpl(AgentMapper agentMapper, SnowflakeIdGenerator idGenerator) {
        this.agentMapper = agentMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public Agent createAgent(String name, String persona, String description, String avatarUrl, Long creatorUserId) {
        if (agentMapper.countByName(name) > 0) {
            throw new BusinessException(400, "智能体名称已被使用");
        }

        Agent agent = new Agent();
        agent.setId(idGenerator.nextId());
        agent.setName(name);
        agent.setPersona(persona);
        agent.setDescription(description != null ? description : "");
        agent.setAvatarUrl(avatarUrl);
        agent.setCreatorUserId(creatorUserId);
        agent.setStatus("ACTIVE");
        agentMapper.insert(agent);
        return agent;
    }

    @Override
    public Agent updateAgent(Long id, String name, String persona, String description, String avatarUrl, Long userId) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new ResourceNotFoundException("智能体不存在");
        }
        if (!agent.getCreatorUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改该智能体");
        }

        if (!agent.getName().equals(name) && agentMapper.countByName(name) > 0) {
            throw new BusinessException(400, "智能体名称已被使用");
        }

        agent.setName(name);
        agent.setPersona(persona);
        agent.setDescription(description != null ? description : "");
        agent.setAvatarUrl(avatarUrl);
        agentMapper.update(agent);
        return agent;
    }

    @Override
    public void deleteAgent(Long id, Long userId) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new ResourceNotFoundException("智能体不存在");
        }
        if (!agent.getCreatorUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该智能体");
        }
        agentMapper.updateStatus(id, "DELETED");
    }

    @Override
    public Agent getAgent(Long id) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new ResourceNotFoundException("智能体不存在");
        }
        return agent;
    }

    @Override
    public Agent findByName(String name) {
        return agentMapper.selectByName(name);
    }

    @Override
    public List<Agent> listAllActive() {
        return agentMapper.selectAllActive();
    }

    @Override
    public List<Agent> listMyAgents(Long userId) {
        return agentMapper.selectByCreatorUserId(userId);
    }
}
