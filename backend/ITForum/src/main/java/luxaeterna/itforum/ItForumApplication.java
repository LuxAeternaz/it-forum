package luxaeterna.itforum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("luxaeterna.itforum.mapper")
@EnableScheduling
@EnableAsync
public class ItForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItForumApplication.class, args);
    }
}
