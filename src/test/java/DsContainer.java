import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class DsContainer implements BeforeAllCallback, AfterAllCallback {

    private static final AtomicBoolean started = new AtomicBoolean(false);

    private static final File dockerfile = new File("src/test/resources/Dockerfile");

    private static final ImageFromDockerfile image = new ImageFromDockerfile()
            .withDockerfile(dockerfile.toPath());

    private static final GenericContainer<?> jbossContainer = new GenericContainer<>(image)
            .withExposedPorts(8080, 9990)
            .withEnv("WILDFLY_HOME", "/opt/bitnami/wildfly")
            .withEnv("WILDFLY_USER", "user")
            .withEnv("WILDFLY_PASSWORD", "password")
            .withEnv("WILDFLY_ADMIN_USER", "admin")
            .withEnv("WILDFLY_ADMIN_PASSWORD", "password")
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*WFLYSRV0051: Admin console listening on http://0.0.0.0:9990.*")
            );


    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if(started.get()) {
            jbossContainer.stop();
            started.set(false);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if(!started.get()) {
            jbossContainer.start();
            System.setProperty("wildfly.management.port", String.valueOf(jbossContainer.getMappedPort(9990)));
            System.setProperty("wildfly.management.address", jbossContainer.getHost());
            System.setProperty("wildfly.port", String.valueOf(jbossContainer.getMappedPort(8080)));
            System.setProperty("wildfly.address", jbossContainer.getHost());
            started.set(true);
        }
    }
}
