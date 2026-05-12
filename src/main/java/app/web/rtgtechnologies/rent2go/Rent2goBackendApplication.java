package app.web.rtgtechnologies.rent2go;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Rent2goBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Rent2goBackendApplication.class, args);
    }

}
