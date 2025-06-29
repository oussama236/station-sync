package tn.spring.stationsync.Services;

import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.BanqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public  class BanqueServiceImpl implements IBanqueService {

    @Autowired
    private BanqueRepository bankRepository;

    @Override
    public Banque saveBank(Banque bank) {
        bank.setStatut(Statut.VIDE); // Toujours initialisé à VIDE
        return bankRepository.save(bank);
    }

    @Override
    public List<Banque> getAllBanks() {
        return bankRepository.findAll();
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
        bankRepository.save(b);
        return b;
    }

    @Override
    public List<Banque> getFilteredBanks(Station station, List<Statut> statuts) {
        return bankRepository.findByFilters(station, statuts);
    }

    @Override
    public void updateBankStatut(Integer bankId) {
        Banque bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new RuntimeException("Bank operation not found"));

        // On force toujours le statut à OK
        bank.setStatut(Statut.OK);
        bankRepository.save(bank);
    }

}