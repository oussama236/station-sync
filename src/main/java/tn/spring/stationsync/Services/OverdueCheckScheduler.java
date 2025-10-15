package tn.spring.stationsync.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueCheckScheduler.class);

    @Autowired
    private OverdueNotificationService overdueNotificationService;

    // Run daily at 05:00 by default; override with notifications.overdue.cron
    @Scheduled(cron = "${notifications.overdue.cron:0 0 5 * * *}")
    public void generateOverdueNotifications() {
        overdueNotificationService.generateOverdueNotifications();
        log.info("Overdue notification scheduled refresh complete");
    }
}


