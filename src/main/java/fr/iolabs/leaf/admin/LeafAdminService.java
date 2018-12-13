package fr.iolabs.leaf.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.errors.NotFoundException;

@Service
public class LeafAdminService<T extends LeafAccount> {

    @Autowired
    private LeafAccountRepository<T> accountRepository;

    public void addAdmin(String email) {

        T newAdminAccount = this.accountRepository.findAccountByEmail(email);

        if (newAdminAccount == null) {
            throw new NotFoundException();
        }

        newAdminAccount.setAdmin(true);

        this.accountRepository.save(newAdminAccount);
    }

    public void removeAdmin(String email) {
        T newAdminAccount = this.accountRepository.findAccountByEmail(email);

        if (newAdminAccount == null) {
            throw new NotFoundException();
        }

        newAdminAccount.setAdmin(false);

        this.accountRepository.save(newAdminAccount);
    }
}
