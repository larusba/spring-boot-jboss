package it.larus.arquillian;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class WildflyContainer implements BeforeAllCallback, AfterAllCallback {
    private static final AtomicBoolean started = new AtomicBoolean(false);

    private static final File dockerfile = new File("src/test/resources/docker/Dockerfile");

    private static final ImageFromDockerfile image = new ImageFromDockerfile()
            .withDockerfile(dockerfile.toPath());

    private static final Network network;

    private static final GenericContainer<?> jbossContainer;

    private static final PostgreSQLContainer<?> postgresqlContainer;

    static {
        network = Network.newNetwork();

        jbossContainer = new GenericContainer<>(image)
                .withExposedPorts(8080, 9990)
                .withNetwork(network)
                .withNetworkAliases("jboss")
                .withEnv("WILDFLY_HOME", "/opt/bitnami/wildfly")
                .withEnv("WILDFLY_USER", "user")
                .withEnv("WILDFLY_PASSWORD", "password")
                .withEnv("WILDFLY_ADMIN_USER", "admin")
                .withEnv("WILDFLY_ADMIN_PASSWORD", "password")
                .waitingFor(new LogMessageWaitStrategy()
                        .withRegEx(".*WFLYSRV0051: Admin console listening on http://0.0.0.0:9990.*")
                );

        postgresqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.2"))
                .withNetwork(network)
                .withNetworkAliases("postgresql")
                .withExposedPorts(5432);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (started.get()) {
            jbossContainer.stop();
            postgresqlContainer.stop();
            started.set(false);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (!started.get()) {
            jbossContainer.start();
            System.setProperty("wildfly.management.port", String.valueOf(jbossContainer.getMappedPort(9990)));
            System.setProperty("wildfly.management.address", jbossContainer.getHost());
            System.setProperty("wildfly.port", String.valueOf(jbossContainer.getMappedPort(8080)));
            System.setProperty("wildfly.address", jbossContainer.getHost());

            postgresqlContainer.start();

            System.out.println("Postgresql listening at jdbc:postgresql://" + postgresqlContainer.getHost() + ":" + postgresqlContainer.getMappedPort(5432));

            started.set(true);
        }
    }

    public static URL getServiceURL() throws MalformedURLException {
        return new URL("http://" + jbossContainer.getHost() + ":" + jbossContainer.getMappedPort(8080) + "/demo");
    }
}
