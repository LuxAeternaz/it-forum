package luxaeterna.itforum.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AiPolishRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    private String instruction;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
