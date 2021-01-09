package fr.iolabs.leaf.authentication.read;

import java.util.List;

import javax.annotation.Resource;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.*;
import fr.iolabs.leaf.common.annotations.AdminOnly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;

@RestController
@RequestMapping("/api/account")
public class LeafAccountReadController {

    @Resource(name = "coreContext")
    private LeafContext coreContext;
    
    @Autowired
    private LeafAccountRepository accountRepository;

    @CrossOrigin
    @GetMapping("/me")
    public LeafAccountDTO getUser() {
        LeafAccount me = this.coreContext.getAccount();
        return LeafAccountDTO.from(me);
    }

    @CrossOrigin
    @AdminOnly
    @GetMapping("/autocomplete")
    public List<LeafUserDTO> autocomplete(@RequestParam("input") String input) {
    	return LeafUserDTO.fromAll(this.accountRepository.findByUsernameLike(input, PageRequest.of(0, 10)));
    }
}