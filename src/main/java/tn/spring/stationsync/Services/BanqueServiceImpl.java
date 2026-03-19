package tn.spring.stationsync.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.NotificationType;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.BanqueRepository;
import tn.spring.stationsync.Specifications.BanqueSpecification;

import java.time.LocalDate;
import java.util.List;

@Service
public class BanqueServiceImpl implements IBanqueService {

    @Autowired
    private BanqueRepository bankRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Banque saveBank(Banque bank) {
        bank.setStatut(Statut.VIDE);
        return bankRepository.save(bank);
    }

    @Override
    public List<Banque> getAllBanks() {
        return bankRepository.findAllOrderedByIdDesc();
    }

    @Override
    public Banque getBanque(Integer idBanque) {
        return bankRepository.findById(idBanque).get();
    }

    @Override
    public void deleteBank(Integer idBanque) {
        bankRepository.deleteById(idBanque);
    }

    @Override
    public Banque updateBanque(Banque b) {
        Banque saved = bankRepository.save(b);
        if (saved.getStatut() == Statut.OK) {
            notificationService.resolveByRef(NotificationType.BANQUE, saved.getIdBanque());
        }
        return saved;
    }

    @Override
    public List<Banque> getFilteredBanks(Station station, List<Statut> statuts,
                                         LocalDate dateFrom, LocalDate dateTo) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before or equal to dateTo");
        }

        Specification<Banque> specification = Specification
                .where(BanqueSpecification.hasStation(station))
                .and(BanqueSpecification.hasStatuts(statuts))
                .and(BanqueSpecification.dateOperationGreaterThanOrEqual(dateFrom))
                .and(BanqueSpecification.dateOperationLessThanOrEqual(dateTo));

        return bankRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "dateOperation"));
    }

    @Override
    public void updateBankStatut(Integer bankId) {
        Banque bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new RuntimeException("Bank operation not found"));

        bank.setStatut(Statut.OK);
        bankRepository.save(bank);
        notificationService.resolveByRef(NotificationType.BANQUE, bank.getIdBanque());
    }
}