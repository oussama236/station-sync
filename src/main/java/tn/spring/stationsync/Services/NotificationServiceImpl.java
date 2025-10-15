package tn.spring.stationsync.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.stationsync.Entities.Notification;
import tn.spring.stationsync.Entities.NotificationStatus;
import tn.spring.stationsync.Entities.NotificationType;
import tn.spring.stationsync.Repositories.NotificationRepository;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createIfAbsent(NotificationType type, Integer refId, String uniqueKey, String message) {
        return notificationRepository.findByUniqueKey(uniqueKey)
                .orElseGet(() -> {
                    Notification n = new Notification();
                    n.setType(type);
                    n.setRefId(refId);
                    n.setStatus(NotificationStatus.OPEN);
                    n.setMessage(message);
                    n.setCreatedAt(LocalDateTime.now());
                    n.setUniqueKey(uniqueKey);
                    Notification saved = notificationRepository.save(n);
                    log.info("Notification created: type={}, refId={}, key={}", type, refId, uniqueKey);
                    return saved;
                });
    }

    @Override
    public Page<Notification> list(NotificationStatus status, Pageable pageable) {
        if (status == null) {
            return notificationRepository.findAll(pageable);
        }
        return notificationRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public void markRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (n.getStatus() == NotificationStatus.OPEN) {
            n.setStatus(NotificationStatus.READ);
            notificationRepository.save(n);
        }
    }

    @Override
    @Transactional
    public void resolveByRef(NotificationType type, Integer refId) {
        List<Notification> notifications = notificationRepository.findByTypeAndRefIdAndStatusIn(
                type, refId, List.copyOf(EnumSet.of(NotificationStatus.OPEN, NotificationStatus.READ))
        );
        for (Notification n : notifications) {
            n.setStatus(NotificationStatus.RESOLVED);
        }
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
            log.info("Resolved {} notifications for {}:{}", notifications.size(), type, refId);
        }
    }
}


