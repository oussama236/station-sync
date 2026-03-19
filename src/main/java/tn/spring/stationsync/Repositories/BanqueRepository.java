package tn.spring.stationsync.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Statut;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BanqueRepository extends JpaRepository<Banque, Integer>, JpaSpecificationExecutor<Banque> {

    @Query("SELECT b FROM Banque b ORDER BY b.idBanque DESC")
    List<Banque> findAllOrderedByIdDesc();

    List<Banque> findByStatutAndDateOperationLessThanEqual(Statut statut, LocalDate date);
}