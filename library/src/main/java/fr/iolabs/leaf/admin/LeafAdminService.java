package fr.iolabs.leaf.admin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.errors.NotFoundException;

@Service
public class LeafAdminService {

	@Autowired
	private LeafAccountRepository accountRepository;

	public void addAdmin(String newAdminAccountId) {

		Optional<LeafAccount> newAdminAccountOpt = this.accountRepository.findById(newAdminAccountId);

		if (newAdminAccountOpt.isEmpty()) {
			throw new NotFoundException();
		}
		
		LeafAccount newAdminAccount = newAdminAccountOpt.get();

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

	public List<String> listAllAdminsEmail() {
		return this.accountRepository.findByAdminTrue().stream().map((account) -> account.getEmail())
				.collect(Collectors.toList());
	}
}
