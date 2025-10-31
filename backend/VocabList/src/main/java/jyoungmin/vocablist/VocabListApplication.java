package jyoungmin.vocablist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the Vocab List Service.
 * Manages vocabulary lists and words with Japanese language support.
 */
@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"jyoungmin.vocablist", "jyoungmin.vocabcommons"})
public class VocabListApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(VocabListApplication.class, args);
    }

}
