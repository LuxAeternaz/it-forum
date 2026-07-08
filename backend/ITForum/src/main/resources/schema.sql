-- IT论坛完整建表脚本

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    bio VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 1,
    last_login_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 版块分类表
CREATE TABLE IF NOT EXISTS categories (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    post_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 帖子表
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT UNSIGNED PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    content_html LONGTEXT,
    user_id BIGINT UNSIGNED NOT NULL,
    category_id INT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    is_pinned TINYINT(1) NOT NULL DEFAULT 0,
    is_essence TINYINT(1) NOT NULL DEFAULT 0,
    view_count INT UNSIGNED NOT NULL DEFAULT 0,
    comment_count INT UNSIGNED NOT NULL DEFAULT 0,
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    last_replied_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_posts_category_status (category_id, status, created_at),
    INDEX idx_posts_user (user_id, created_at),
    FULLTEXT INDEX ft_posts_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT UNSIGNED PRIMARY KEY,
    content TEXT NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED NOT NULL,
    parent_id BIGINT UNSIGNED,
    root_id BIGINT UNSIGNED,
    reply_to_user_id BIGINT UNSIGNED,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_comments_post_root (post_id, root_id, created_at),
    INDEX idx_comments_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 点赞表
CREATE TABLE IF NOT EXISTS likes (
    id BIGINT UNSIGNED PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE INDEX uq_likes_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 收藏表
CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT UNSIGNED PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE INDEX uq_bookmarks_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 关注表
CREATE TABLE IF NOT EXISTS follows (
    id BIGINT UNSIGNED PRIMARY KEY,
    follower_id BIGINT UNSIGNED NOT NULL,
    followee_id BIGINT UNSIGNED NOT NULL,
    follow_type VARCHAR(20) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE INDEX uq_follows (follower_id, followee_id, follow_type),
    INDEX idx_follows_followee (followee_id, follow_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT UNSIGNED PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(30) NOT NULL,
    sender_id BIGINT UNSIGNED,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(500),
    target_type VARCHAR(20),
    target_id BIGINT UNSIGNED,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_notifications_user_read (user_id, is_read, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 文件上传记录表
CREATE TABLE IF NOT EXISTS uploads (
    id BIGINT UNSIGNED PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT UNSIGNED NOT NULL,
    mime_type VARCHAR(100),
    width INT,
    height INT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. AI智能体应答记录表
CREATE TABLE IF NOT EXISTS ai_agent_responses (
    id BIGINT UNSIGNED PRIMARY KEY,
    trigger_user_id BIGINT UNSIGNED NOT NULL,
    comment_id BIGINT UNSIGNED,
    post_id BIGINT UNSIGNED NOT NULL,
    parent_comment_id BIGINT UNSIGNED,
    prompt TEXT NOT NULL,
    response LONGTEXT NOT NULL,
    model_used VARCHAR(50),
    tokens_used INT UNSIGNED,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 草稿表
CREATE TABLE IF NOT EXISTS drafts (
    id BIGINT UNSIGNED PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    category_id INT UNSIGNED NOT NULL,
    version INT UNSIGNED NOT NULL DEFAULT 1,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE INDEX uq_drafts_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. AI审核日志表
CREATE TABLE IF NOT EXISTS moderation_logs (
    id BIGINT UNSIGNED PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    content_snippet VARCHAR(500) NOT NULL,
    result VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    confidence DECIMAL(5,4),
    reviewed_by BIGINT UNSIGNED,
    reviewed_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. AI智能体表
CREATE TABLE IF NOT EXISTS agents (
    id BIGINT UNSIGNED PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    persona TEXT NOT NULL,
    description VARCHAR(500),
    creator_user_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(500),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_agents_name (name),
    INDEX idx_agents_creator (creator_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE agents ADD COLUMN avatar_url VARCHAR(500) AFTER description;

-- ai_agent_responses 新增 agent_id 列（兼容已有表）
ALTER TABLE ai_agent_responses ADD COLUMN agent_id BIGINT UNSIGNED AFTER parent_comment_id;

-- 初始数据: AI机器人用户
INSERT INTO users (id, username, email, password_hash, role, bio)
VALUES (1, 'IT小助手', 'ai-assistant@itforum.internal', '', 'ADMIN', '我是ITForum的AI助手，可以@我提问技术问题')
ON DUPLICATE KEY UPDATE username=username;

-- 初始数据: 默认AI智能体
INSERT INTO agents (id, name, persona, description, creator_user_id, status, created_at, updated_at)
VALUES (1, 'IT小助手', '你是"IT小助手"，IT技术论坛的官方AI助手。你的性格：友好、耐心、对技术充满热情。技术回答专业准确，不确定时会坦诚说明。主要用中文回答，讨论代码/技术术语时可使用英文。你的能力：回答编程和技术问题，用简单语言解释复杂概念，提供代码示例（使用markdown代码块格式），建议最佳实践和设计模式，帮助调试和排查技术问题。限制：你不是人类，需要时请表明自己是AI。无法访问外部URL或实时数据。知识有截止日期。', 'ITForum官方AI助手，可在评论中@提及提问', 1, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=name;

-- 初始数据: 默认版块
INSERT INTO categories (name, description, icon, sort_order) VALUES
('Java', 'Java编程语言、Spring框架、JVM技术讨论', 'Coffee', 1),
('前端开发', 'Vue、React、Angular、JavaScript/TypeScript', 'Monitor', 2),
('Python', 'Python语言、数据分析、机器学习', 'DataAnalysis', 3),
('AI与大模型', 'LLM、LangChain、AIGC技术讨论', 'Cpu', 4),
('数据库', 'MySQL、Redis、Elasticsearch等数据存储技术', 'Coin', 5),
('程序员闲聊', '职业发展、技术趋势、行业八卦', 'ChatDotRound', 6)
ON DUPLICATE KEY UPDATE name=name;
