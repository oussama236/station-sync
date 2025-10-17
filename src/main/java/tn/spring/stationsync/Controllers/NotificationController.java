package tn.spring.stationsync.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Entities.Notification;
import tn.spring.stationsync.Entities.NotificationStatus;
import tn.spring.stationsync.Entities.NotificationType;
import tn.spring.stationsync.Services.NotificationService;
import tn.spring.stationsync.Services.OverdueNotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private OverdueNotificationService overdueNotificationService;

    @GetMapping
    public Page<Notification> list(@RequestParam(required = false) NotificationStatus status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "1000") int size) {
        return notificationService.list(status, PageRequest.of(page, size));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{type}/{refId}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable NotificationType type, @PathVariable Integer refId) {
        notificationService.resolveByRef(type, refId);
        return ResponseEntity.ok().build();
    }

    // Trigger a manual refresh of overdue notifications (used after login or on demand)
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshOverdues() {
        overdueNotificationService.generateOverdueNotifications();
        return ResponseEntity.ok().build();
    }

    // ✅ Unread/any status count for the bell
    @GetMapping("/count")
    public long count(@RequestParam(required = false) NotificationStatus status) {
        return notificationService.count(status); // implement in service/repo
    }


    // NotificationController.java
    @GetMapping("/dropdown")
    public java.util.List<Notification> dropdown(@RequestParam(defaultValue = "1000") int size) {
        var statuses = java.util.List.of(NotificationStatus.OPEN, NotificationStatus.READ);
        var pageable = org.springframework.data.domain.PageRequest.of(0, Math.min(size, 1000));
        return notificationService.listByStatuses(statuses, pageable).getContent();
    }

}


