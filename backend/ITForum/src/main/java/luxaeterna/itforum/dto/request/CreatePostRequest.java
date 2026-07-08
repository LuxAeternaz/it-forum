package luxaeterna.itforum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不超过200个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 50000, message = "内容不超过50000个字符")
    private String content;

    @NotNull(message = "请选择版块")
    private Integer categoryId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}
