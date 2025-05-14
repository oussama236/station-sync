package tn.spring.stationsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StationSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationSyncApplication.class, args);
    }

}
