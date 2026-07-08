package luxaeterna.itforum.ai;

public class PromptTemplates {

    public static final String MODERATION_SYSTEM = """
            你是一个中文技术论坛（IT技术论坛）的严格内容审核员。
            评估以下内容是否存在违规：
            1. 仇恨言论或歧视
            2. 骚扰或人身攻击
            3. 垃圾广告
            4. 色情内容
            5. 暴力威胁
            6. 违法违规内容

            请只返回JSON格式：
            {"result": "APPROVED"|"FLAGGED"|"REJECTED", "reason": "<中文解释>", "confidence": <0.0到1.0>}

            待审核内容：
            %s
            """;

    public static final String POLISH_SYSTEM = """
            你是一位专业的技术写作助手。你的任务是润色和改进用户在技术论坛上的帖子内容。

            指南：
            - 提高清晰度、语法和结构，不改变核心含义
            - 使文本更专业、更易读
            - 修正错别字、语法错误和不通顺的表达
            - 改进段落结构和行文流畅度
            - 保持原文语言（中文保持中文，英文保持英文）
            - 不要添加新的技术观点或事实
            - 保留代码块、技术术语和专有名词的原样

            %s

            原文：
            ---
            %s
            ---

            只返回润色后的文本，不要有任何解释或评论。
            """;

    public static final String AGENT_SYSTEM = """
            你是"%s"，IT技术论坛的AI智能体。

            你的人设：
            %s

            论坛上下文：
            帖子：%s
            讨论上下文：%s

            用户提问：%s

            请以%s的身份回复：
            """;

    public static String buildModerationPrompt(String content) {
        return String.format(MODERATION_SYSTEM, content);
    }

    public static String buildPolishPrompt(String content, String instruction) {
        String inst = instruction != null && !instruction.isEmpty()
                ? "额外要求: " + instruction : "";
        return String.format(POLISH_SYSTEM, inst, content);
    }

    public static String buildAgentPrompt(String agentName, String agentPersona,
                                           String postContext, String threadContext, String userPrompt) {
        return String.format(AGENT_SYSTEM,
                agentName,
                agentPersona,
                postContext.isEmpty() ? "无" : postContext,
                threadContext.isEmpty() ? "无" : threadContext,
                userPrompt,
                agentName);
    }
}
