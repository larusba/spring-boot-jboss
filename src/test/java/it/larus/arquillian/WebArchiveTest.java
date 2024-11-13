package it.larus.arquillian;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.classic.methods.HttpGet;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;


@ExtendWith(WildflyContainer.class)
@ExtendWith(ArquillianExtension.class)
public class WebArchiveTest {

    @ArquillianResource
    private Deployer deployer;

    private Boolean deployed = false;

    /**
     * Collect all the dependencies from the pom.xml
     *
     * @return an array of jars (as files)
     */
    private static File[] libraries() {
        return Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();
    }

    /**
     * Create the WAR (web archive) which contains:
     *  - the sources for the JAX-RS and the Spring Rest classes
     *  - the jboss-web.xml configuration
     *  - an empty beans.xml
     *  - the additional datasource configuration (postgresql-ds.xml)
     *  - the Arquillian configuration for deployment
     *
     * @return a ShrinkWrap Archive that represents the WAR to be deployed
     */
    private static Archive<WebArchive> createWebArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "spring.war")
                .addAsLibraries(libraries())
                // Java JAX-RS Classes
                .addPackages(true, "it.larus.arquillian.javax")
                // Spring Boot Classes
                .addPackages(true, "it.larus.arquillian.spring")
                .addAsWebInfResource("webapp/WEB-INF/jboss-web.xml")
                .addAsManifestResource(
                        new ByteArrayAsset("<beans/>".getBytes()),
                        ArchivePaths.create("beans.xml")
                )
                .addAsWebInfResource("webapp/WEB-INF/postgresql-ds.xml")
                .addAsResource("arquillian.xml");
    }

    /**
     * An alternative version for deployment with an EAR (enterprise archive)
     *
     * @return a ShrinkWrap Archive that represents the EAR to be deployed
     */
    private static Archive<EnterpriseArchive> createEnterpriseArchive() {
        return ShrinkWrap.create(EnterpriseArchive.class)
                .addAsLibraries(libraries())
                .setApplicationXML("application.xml")
                .addAsModule(createWebArchive());
    }

    @Deployment(name = "demo", managed = false)
    public static Archive<WebArchive> createArchive() {
        // EAR version
        /*return createEnterpriseArchive();*/

        return createWebArchive();
    }

    @Test
    public void test() {
        if (!deployed) {
            System.out.println("deploying webapp");
            deployer.deploy("demo");
            deployed = true;
        }

        System.out.println("Admin console at http://localhost:" + System.getProperty("wildfly.management.port"));

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // ----- Java JAX-RS
            System.out.println("Running Java JAX-RS Tests");
            // ----- TEST 1 -----
            HttpGet httpGet = new HttpGet(WildflyContainer.getServiceURL() + "/legacy-app/test/datetime");
            CloseableHttpResponse response = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response.getCode(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/legacy-app/test/datetime"
            );

            String content = new String(response.getEntity().getContent().readAllBytes());
            System.out.println(content);

            // ----- TEST 2 -----
            httpGet = new HttpGet(WildflyContainer.getServiceURL() + "/legacy-app/test/datasource/ExampleDS");
            response = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response.getCode(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/legacy-app/test/datasource/ExampleDS"
            );

            content = new String(response.getEntity().getContent().readAllBytes());
            System.out.println(content);

            // ----- TEST 3 -----
            httpGet = new HttpGet(WildflyContainer.getServiceURL() + "/legacy-app/test/datasource/PostgresDS");
            response = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response.getCode(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/legacy-app/test/datasource/PostgresDS"
            );

            content = new String(response.getEntity().getContent().readAllBytes());
            System.out.println(content);

            // ----- Spring Boot RestController
            System.out.println("\nRunning Spring Boot Tests");
            // ----- TEST 4 -----
            httpGet = new HttpGet(WildflyContainer.getServiceURL() + "/spring-app/datetime");
            response = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response.getCode(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/spring-app/datetime"
            );

            content = new String(response.getEntity().getContent().readAllBytes());
            System.out.println(content);

            // ----- TEST 5 -----
            httpGet = new HttpGet(WildflyContainer.getServiceURL() + "/spring-app/datasource/ExampleDS");
            response = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response.getCode(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/spring-app/datasource/ExampleDS"
            );

            content = new String(response.getEntity().getContent().readAllBytes());
            System.out.println(content);

            // ----- TEST 6 -----
            httpGet = new HttpGet(WildflyContainer.getServiceURL() + "/spring-app/datasource/PostgresDS");
            response = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response.getCode(),
                    "Incorrect response code from " + WildflyContainer.getServiceURL() + "/spring-app/datasource/PostgresDS"
            );

            content = new String(response.getEntity().getContent().readAllBytes());
            System.out.println(content);

        } catch (IOException e) {
            Assertions.fail(e.getMessage(), e);
        }
    }
}
