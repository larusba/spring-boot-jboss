package it.larus.arquillian.javax;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

@Path("/test")
public class LegacyTestResource {

    @GET
    @Path("/datetime")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLocalTime() {
        return LocalDateTime.now().toString();
    }

    @GET
    @Path("/datasource/{dataSourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getDataSource(@PathParam("dataSourceName") String dataSourceName) {
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
