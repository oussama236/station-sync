package tn.spring.stationsync.Services;

import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;

import java.util.List;

public interface IBanqueService {

    // Create a Bank
    Banque saveBank(Banque bank);

    // Retrieve all Banks
    List<Banque> getAllBanks();

    // Find a Bank by ID
    Banque getBanque (Integer idBanque);


    // Delete a Bank by ID
    void deleteBank(Integer idBanque);

    //Update Bank by ID
    Banque updateBanque (Banque  b);

    List<Banque> getFilteredBanks(Station station, List<Statut> statuts);

    void updateBankStatut(Integer bankId);

    // Dans IBanqueService.java

}
