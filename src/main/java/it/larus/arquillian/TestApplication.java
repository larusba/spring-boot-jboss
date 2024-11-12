package it.larus.arquillian;

//import jakarta.ws.rs.core.Application;
//import jakarta.ws.rs.ApplicationPath;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
//@ApplicationPath("app")
public class TestApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(TestApplication.class);
    }
}
