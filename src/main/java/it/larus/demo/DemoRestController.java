package it.larus.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DemoRestController {

    private final DummyService dummyService = DummyService.getInstance();

    @GetMapping("hello")
    public ResponseEntity<String> service() {
        return ResponseEntity.ok().body(dummyService.service());
    }
}