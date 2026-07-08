package luxaeterna.itforum.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private String stripLeadingComments(String sql) {
        String[] lines = sql.split("\n");
        int i = 0;
        for (; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("--")) {
                break;
            }
        }
        if (i >= lines.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int j = i; j < lines.length; j++) {
            sb.append(lines[j]).append("\n");
        }
        return sb.toString().trim();
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("schema.sql");
            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }
            }

            String[] statements = sql.toString().split(";");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    String trimmed = stripLeadingComments(statement.trim());
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (Exception e) {
                            log.debug("SQL statement skipped (may already exist): {}", e.getMessage());
                        }
                    }
                }
            }
            log.info("Database schema initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
        }
    }
}
