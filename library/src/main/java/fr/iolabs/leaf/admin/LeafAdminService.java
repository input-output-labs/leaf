package fr.iolabs.leaf.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.errors.NotFoundException;

@Service
public class LeafAdminService {

    @Autowired
    private LeafAccountRepository accountRepository;

    public void addAdmin(String email) {

    	LeafAccount newAdminAccount = this.accountRepository.findAccountByEmail(email);

        if (newAdminAccount == null) {
            throw new NotFoundException();
        }

        newAdminAccount.setAdmin(true);

        this.accountRepository.save(newAdminAccount);
    }

    public void removeAdmin(String email) {
    	LeafAccount newAdminAccount = this.accountRepository.findAccountByEmail(email);

        if (newAdminAccount == null) {
            throw new NotFoundException();
        }

        newAdminAccount.setAdmin(false);

        this.accountRepository.save(newAdminAccount);
    }
}
