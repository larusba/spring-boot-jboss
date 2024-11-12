package it.larus.arquillian;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestResourceController {


    @GetMapping("/datetime")
    public LocalDateTime getProperties() {
        return LocalDateTime.now();
    }

    @GetMapping("/datasource")
    public Map<String, String> getDataSource() {
        try {
            InitialContext initialContext = new InitialContext();
            DataSource exampleDS = (DataSource) initialContext.lookup("java:jboss/datasources/ExampleDS");
            Connection connection = exampleDS.getConnection();

            return Map.of("java:jboss/datasources/ExampleDS", connection.getMetaData().getDatabaseProductVersion());
        } catch (NamingException e) {
            System.out.println("Datasource non trovato");
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.out.println("Connessione fallita");
            throw new RuntimeException(e);
        }
    }
}
