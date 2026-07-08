package luxaeterna.itforum.dto.response;

public class MentionSuggestion {

    private String id;
    private String name;
    private String avatarUrl;
    private String type; // "agent" or "user"

    public MentionSuggestion() {}

    public MentionSuggestion(String id, String name, String avatarUrl, String type) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
