package tn.spring.stationsync.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.spring.stationsync.Entities.Notification;
import tn.spring.stationsync.Entities.NotificationStatus;
import tn.spring.stationsync.Entities.NotificationType;

public interface NotificationService {

    Notification createIfAbsent(NotificationType type, Integer refId, String uniqueKey, String message);

    Page<Notification> list(NotificationStatus status, Pageable pageable);

    void markRead(Long id);

    void resolveByRef(NotificationType type, Integer refId);
}


