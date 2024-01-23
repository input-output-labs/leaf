package fr.iolabs.leaf.eligibilities;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class LeafAdministrationEligibilitiesComposer implements ApplicationListener<LeafEligibilitiesEvent> {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Override
    public void onApplicationEvent(LeafEligibilitiesEvent event) {
    	LeafAccount account = coreContext.getAccount();
		this.getEligibilities(account, event.eligibilities(), event.eligibilityKey());
    }
	
	public void getEligibilities(LeafAccount account, Map<String, LeafEligibility> eligibilities, List<String> eligibilityKeys) {
		if (eligibilityKeys == null || eligibilityKeys.contains("seeAdmin")) {
			LeafEligibility canAccessAdminPage = new LeafEligibility(account != null && account.isAdmin());
			if (!canAccessAdminPage.eligible) {
				canAccessAdminPage.reasons.add("Not an admin");
			}
			eligibilities.put("seeAdmin", canAccessAdminPage);
		}
	}
}
