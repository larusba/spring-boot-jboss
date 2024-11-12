package it.larus.arquilian;


import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(WildflyContainer.class)
@ExtendWith(ArquillianExtension.class)
//@SpringBootTest
public class WebArchiveTest {
    
    @ArquillianResource
    private Deployer deployer;

    private Boolean deployed = false;
    
    @BeforeClass 
    public static void setUp() throws MavenInvocationException {
       // Create an invocation request
    }

    @Deployment(name = "demo", managed = false)
    public static Archive<WebArchive> createArchive() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("mvn", "clean", "package", "-DskipTests");
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        // Assert that the exit code is 0 (indicating successful execution)
        assertEquals(0, exitCode, "Maven build failed");
       
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/arquillian-spring-1.0.0-SNAPSHOT-jboss-full.war"));
    }

    @Test
    public void test() throws MalformedURLException {
        try {
            if (!deployed) {
                System.out.println("deploying webapp");
                deployer.deploy("demo");
                deployed = true;
            }
        } catch (Exception e) {
            System.out.println("e = " + e);
        }

        System.out.println("Admin console at http://localhost:" + System.getProperty("wildfly.management.port"));

        try (Client client = ClientBuilder.newClient()) {
            // client.target(WildflyContainer.getServiceURL() + "/test/datetime").request().get()
            WebTarget target = client.target(WildflyContainer.getServiceURL() + "/test/datetime");
            Response response = target.request().get();

            assertEquals(
                    200,
                    response.getStatus(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/test/datetime"
            );

            String text = response.readEntity(String.class);
            Assertions.assertNotNull(text);

            System.out.println(text);

            target = client.target(WildflyContainer.getServiceURL() + "/test/datasource");
            response = target.request().get();

            assertEquals(
                    200,
                    response.getStatus(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/test/datasource"
            );

            text = response.readEntity(String.class);
            Assertions.assertNotNull(text);

            System.out.println(text);
        }
    }
}
