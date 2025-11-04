package vaultWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main entry point for the backend application.
 * <p>
 * This class bootstraps the Spring Boot application and configures essential
 * Spring features such as Aspect-Oriented Programming (AOP) support.
 * </p>
 *
 * <p>Key Annotations:
 * <ul>
 *     <li>{@link SpringBootApplication} – Marks this class as a Spring Boot application and enables
 *     component scanning, auto-configuration, and property support.</li>
 *     <li>{@link EnableAspectJAutoProxy} – Enables support for handling components marked with
 *     AspectJ's @Aspect annotation for AOP functionality.</li>
 * </ul>
 * </p>
 *
 * <p>Implements {@link WebMvcConfigurer} to allow optional customization of Spring MVC configuration.</p>
 */
@EnableAspectJAutoProxy
@SpringBootApplication
@EnableAutoConfiguration
public class BackendApplication implements WebMvcConfigurer {

    /**
     * Main method that serves as the entry point of the Spring Boot application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}