package tn.spring.stationsync.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.spring.stationsync.Entities.*;
import tn.spring.stationsync.Repositories.BanqueRepository;
import tn.spring.stationsync.Repositories.ShellRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OverdueNotificationServiceImpl implements OverdueNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OverdueNotificationServiceImpl.class);

    @Autowired
    private ShellRepository shellRepository;

    @Autowired
    private BanqueRepository banqueRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IShellService shellService;

    @Override
    public void generateOverdueNotifications() {
        // Keep shell statuts coherent first
        shellService.updateStatutsWhenPrelevementDue();

        LocalDate cutoff = LocalDate.now().minusDays(5);
        List<Notification> notifications = new ArrayList<>();
        List<Shell> shells = shellRepository.findByStatutAndDatePrelevementLessThanEqual(Statut.EN_ATTENTE, cutoff);
        for (Shell s : shells) {
            String key = "SHELL:" + s.getIdShell() + ":EN_ATTENTE_5D";
            String msg = "Facture " + s.getNumeroFacture() + "  est en attente pour plus de 5 jours (depuis " + s.getDatePrelevement() + ")";
            notifications.add(notificationService.createIfAbsent(NotificationType.SHELL, s.getIdShell(), key, msg));
        }

        List<Banque> banques = banqueRepository.findByStatutAndDateOperationLessThanEqual(Statut.VIDE, cutoff);
        for (Banque b : banques) {
            String key = "BANQUE:" + b.getIdBanque() + ":VIDE_5D";
            String msg = "Banque op " + b.getNumeroBordereau() + " est vide pour plus de 5 jours (depuis " + b.getDateOperation() + ")";
            notificationService.createIfAbsent(NotificationType.BANQUE, b.getIdBanque(), key, msg);
        }

        notifications.forEach((notif -> {
            log.info("notif" + notif.getMessage());
        }));
        log.info("On-demand overdue notification refresh: shells={}, banques={}", shells.size(), banques.size());
    }
}


