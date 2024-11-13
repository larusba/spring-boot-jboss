package it.larus.arquillian.spring;

import org.springframework.web.bind.annotation.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(path = "/spring-app")
public class SpringTestResource {

    @GetMapping(path = "/datetime")
    public String getLocalTime() {
        return LocalDateTime.now().toString();
    }

    @GetMapping(path="/datasource/{dataSourceName}")
    public Map<String, String> getDataSource(@PathVariable("dataSourceName") String dataSourceName) {
        try {
            InitialContext initialContext = new InitialContext();
            final String name = "java:jboss/datasources/" + dataSourceName;
            System.out.println("LegacyTestResource.getDataSource - looking for " + name);
            DataSource dataSource = (DataSource) initialContext.lookup(name);

            try (Connection connection = dataSource.getConnection()) {
                return Map.of(name, connection.getMetaData().getDatabaseProductVersion());
            }
        } catch (NamingException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
