package fr.iolabs.leaf.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.statistics.LeafStatisticGatheringEvent;

@Component
public class Account_StatisticGatheringEventListener implements ApplicationListener<LeafStatisticGatheringEvent> {

	@Autowired
	private LeafAccountRepository accountRepository;

	@Override
	public void onApplicationEvent(LeafStatisticGatheringEvent event) {
		event.gatherer().postStatistic("accountsCount", this.accountRepository.count());
	}

}
