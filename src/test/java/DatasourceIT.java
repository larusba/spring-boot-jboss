import it.larus.demo.DummyService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

@ExtendWith(DsContainer.class)
@ExtendWith(ArquillianExtension.class)
class DatasourceIT {

    @Deployment
    static WebArchive createDeployment() {
        System.out.println("wildfly.address: " + System.getProperty("wildfly.address"));
        System.out.println("wildfly.port: " + System.getProperty("wildfly.port"));
        System.out.println("wildfly.management.address: " + System.getProperty("wildfly.management.address"));
        System.out.println("wildfly.management.port: " + System.getProperty("wildfly.management.port"));
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .withTransitivity().asFile();

        WebArchive war = ShrinkWrap.create(WebArchive.class, "testDS.war")
                .addPackages(true, "it.larus.demo")
                .addAsLibraries(libs)
                .addAsResource("arquillian.xml")
                .addAsResource("WEB-INF/jboss-web.xml");

        war.as(ZipExporter.class).exportTo(new File("target/testDS.war"), true);

        return war;
    }

    @Test
    void testJndi () {
        try {
            DummyService ds = DummyService.getInstance();
            String config = ds.service();
            Assertions.assertNotNull(config);
            System.out.println(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
