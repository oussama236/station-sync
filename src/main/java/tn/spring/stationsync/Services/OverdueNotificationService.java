package tn.spring.stationsync.Services;

public interface OverdueNotificationService {
    /**
     * Recompute shell statuts if needed and generate overdue notifications for Shell and Banque.
     * Safe to call repeatedly; idempotent per uniqueKey.
     */
    void generateOverdueNotifications();
}


