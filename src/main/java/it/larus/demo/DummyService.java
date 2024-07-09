package it.larus.demo;

import org.springframework.stereotype.Service;

public class DummyService {

    private static DummyService INSTANCE;

    public static DummyService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DummyService();
        }

        return INSTANCE;
    }

    public String service() {
        return "Hello World";
    }
}
