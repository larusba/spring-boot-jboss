package it.larus.demo;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

public class DummyService {

    private static DummyService INSTANCE;

    private final String input;

    private DummyService(String input){
        this.input = input;
    }

    public static DummyService getInstance() {
        if (INSTANCE == null) {
            try {
                final DataSource dataSource = InitialContext.doLookup("java:jboss/datasources/ExampleDS");
                try (final Connection connection = dataSource.getConnection()) {
                    INSTANCE = new DummyService(connection.getMetaData().getDatabaseProductVersion());
                }

            } catch (Exception e) {
                INSTANCE = new DummyService(e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        }

        return INSTANCE;
    }

    public String service() {
        return this.input;
    }
}