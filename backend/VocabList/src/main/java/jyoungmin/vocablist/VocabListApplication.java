package jyoungmin.vocablist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VocabListApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabListApplication.class, args);
    }

}
