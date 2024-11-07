import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

@ExtendWith(DsContainer.class)
@ExtendWith(ArquillianExtension.class)
class DatasourceIT {

    @ArquillianResource
    private Deployer deployer;

    @Deployment(name = "testDS", managed = false)
    static WebArchive createDeployment() {
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .withTransitivity().asFile();

        WebArchive war = ShrinkWrap.create(WebArchive.class, "testDS.war")
                .addPackages(true, "it.larus.demo")
                .addClass(DatasourceIT.class)
                .addAsLibraries(libs)
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("arquillian.xml");
        return war;
    }

    @Test
    void testJndi () {
        deployer.deploy("testDS");
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(DsContainer.getContainerUrl());
            CloseableHttpResponse response1 = httpclient.execute(httpGet);

            Assertions.assertEquals(
                    200,
                    response1.getStatusLine().getStatusCode()
            );
            String response = new String(response1.getEntity().getContent().readAllBytes());
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            deployer.undeploy("testDS");
        }
    }
}
