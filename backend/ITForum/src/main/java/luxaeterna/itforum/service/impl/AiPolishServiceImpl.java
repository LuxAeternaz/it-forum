package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.ai.PolishAgent;
import luxaeterna.itforum.service.AiPolishService;
import org.springframework.stereotype.Service;

@Service
public class AiPolishServiceImpl implements AiPolishService {

    private final PolishAgent polishAgent;

    public AiPolishServiceImpl(PolishAgent polishAgent) {
        this.polishAgent = polishAgent;
    }

    @Override
    public String polish(String content, String instruction) {
        return polishAgent.polish(content, instruction);
    }
}
