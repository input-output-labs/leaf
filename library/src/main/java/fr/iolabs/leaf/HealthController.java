package fr.iolabs.leaf;

import javax.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @CrossOrigin
    @PermitAll
    @GetMapping
    public ResponseEntity<String> apiHealth() {
        return ResponseEntity.ok().build();
    }
}
