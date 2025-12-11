package fr.iolabs.leaf.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.scheduler.model.Frequency;

/**
 * Service responsible for scheduling and identifying applicable frequencies.
 * Runs every 5 minutes and determines which frequency-based should be executed.
 */
@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private final ApplicationEventPublisher eventPublisher;

    public SchedulerService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes every 5 minutes to identify and publish applicable healthcheck frequencies.
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void executeHealthchecks() {
        LocalDateTime now = LocalDateTime.now();
        List<Frequency> applicableFrequencies = identifyApplicableFrequencies(now);

        logger.debug("Applicable frequencies at {}: {}", now, applicableFrequencies);

        // Emit Spring Boot event with the frequencies
        if (!applicableFrequencies.isEmpty()) {
            SchedulerFrequenciesEvent event = new SchedulerFrequenciesEvent(this, applicableFrequencies);
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * Identifies which frequencies are applicable at the given time.
     *
     * @param now The current date and time
     * @return List of applicable frequencies
     */
    private List<Frequency> identifyApplicableFrequencies(LocalDateTime now) {
        List<Frequency> frequencies = new ArrayList<>();
        int hour = now.getHour();
        int minute = now.getMinute();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int dayOfMonth = now.getDayOfMonth();

        // 5 minutes: always applicable (runs every 5 minutes)
        frequencies.add(Frequency.FIVE_MINUTES);

        // 30 minutes: at :00 and :30
        if (minute == 0 || minute == 30) {
            frequencies.add(Frequency.THIRTY_MINUTES);
        }

        // 1 hour: at :00 of each hour
        if (minute == 0) {
            frequencies.add(Frequency.ONE_HOUR);
        }

        // 2 hours: at 00:00, 02:00, 04:00, 06:00, 08:00, 10:00, 12:00, 14:00, 16:00, 18:00, 20:00, 22:00
        if (minute == 0 && hour % 2 == 0) {
            frequencies.add(Frequency.TWO_HOURS);
        }

        // 6 hours: at 00:00, 06:00, 12:00, 18:00
        if (minute == 0 && (hour == 0 || hour == 6 || hour == 12 || hour == 18)) {
            frequencies.add(Frequency.SIX_HOURS);
        }

        // 12 hours: at 00:00 and 12:00
        if (minute == 0 && (hour == 0 || hour == 12)) {
            frequencies.add(Frequency.TWELVE_HOURS);
        }

        // 1 day: at 00:00
        if (minute == 0 && hour == 0) {
            frequencies.add(Frequency.DAILY);

            // Weekly: every Monday at 00:00
            if (dayOfWeek == DayOfWeek.MONDAY) {
                frequencies.add(Frequency.WEEKLY);
            }

            // Monthly: first day of the month at 00:00
            if (dayOfMonth == 1) {
                frequencies.add(Frequency.MONTHLY);
            }
        }

        return frequencies;
    }
}
