package fr.iolabs.leaf;

import javax.annotation.security.PermitAll;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
public class LeafController {

    @PermitAll
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public String listAllAuthorizedEmails() {
        return "UP and RUNNING";
    }
}
