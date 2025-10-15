package tn.spring.stationsync.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.spring.stationsync.Entities.Notification;
import tn.spring.stationsync.Entities.NotificationStatus;
import tn.spring.stationsync.Entities.NotificationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByUniqueKey(String uniqueKey);

    List<Notification> findByTypeAndRefIdAndStatusIn(NotificationType type, Integer refId, List<NotificationStatus> statuses);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Page<Notification> findAll(Pageable pageable);
}


