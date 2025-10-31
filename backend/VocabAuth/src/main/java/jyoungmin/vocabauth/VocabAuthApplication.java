package jyoungmin.vocabauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the Vocab Authentication Service.
 * Handles user registration, login, JWT token management, and authentication.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"jyoungmin.vocabauth", "jyoungmin.vocabcommons"})
public class VocabAuthApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(VocabAuthApplication.class, args);
    }

}
