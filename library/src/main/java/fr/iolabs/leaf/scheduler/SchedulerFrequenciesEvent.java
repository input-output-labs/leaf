package fr.iolabs.leaf.scheduler;

import java.util.List;
import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.scheduler.model.Frequency;

/**
 * Spring application event published when scripts frequencies are identified.
 * Listeners can subscribe to this event to execute scripts for the applicable frequencies.
 */
public class SchedulerFrequenciesEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final List<Frequency> frequencies;

    /**
     * Creates a new SchedulerFrequenciesEvent.
     *
     * @param source The object that published this event (typically SchedulerService)
     * @param frequencies The list of frequencies that are applicable at the current time
     */
    public SchedulerFrequenciesEvent(Object source, List<Frequency> frequencies) {
        super(source);
        this.frequencies = frequencies;
    }

    /**
     * Returns the list of applicable frequencies.
     *
     * @return List of frequencies that should trigger scripts
     */
    public List<Frequency> getFrequencies() {
        return frequencies;
    }
}
