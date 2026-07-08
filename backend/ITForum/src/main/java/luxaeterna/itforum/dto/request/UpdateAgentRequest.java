package luxaeterna.itforum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateAgentRequest {

    @NotBlank(message = "智能体名称不能为空")
    @Size(max = 50, message = "名称最长50个字符")
    @Pattern(regexp = "^[\\w\\u4e00-\\u9fff]+$", message = "名称不能包含空格或特殊字符")
    private String name;

    @NotBlank(message = "人设不能为空")
    @Size(max = 5000, message = "人设最长5000个字符")
    private String persona;

    @Size(max = 500, message = "描述最长500个字符")
    private String description;

    @Size(max = 500, message = "头像URL最长500个字符")
    private String avatarUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
