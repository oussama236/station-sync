package tn.spring.stationsync.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.spring.stationsync.Entities.Prelevement;

import java.time.LocalDate;
import java.util.List;


public interface PrelevementRepository extends JpaRepository<Prelevement, Integer> {

    @Query("SELECT p FROM Prelevement p " +
            "WHERE (:date IS NULL OR p.dateOperation = :date) " +
            "AND (:montant IS NULL OR p.montant = :montant)")
    List<Prelevement> searchPrelevements(
            @Param("date") LocalDate date,
            @Param("montant") Double montant
    );
}
