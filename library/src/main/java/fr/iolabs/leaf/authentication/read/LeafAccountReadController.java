package fr.iolabs.leaf.authentication.read;

import javax.annotation.Resource;

import fr.iolabs.leaf.authentication.model.*;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;

@RestController
@RequestMapping("/api/account")
public class LeafAccountReadController {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @CrossOrigin
    @GetMapping("/me")
    public LeafAccountDTO getUser() {
        LeafAccount me = this.coreContext.getAccount();
        return LeafAccountDTO.from(me);
    }
}