package tn.spring.stationsync.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.spring.stationsync.Entities.Prelevement;

import java.time.LocalDate;
import java.util.List;

public interface PrelevementRepository extends JpaRepository<Prelevement, Integer>, JpaSpecificationExecutor<Prelevement> {

    List<Prelevement> findByDateOperation(LocalDate dateOperation);

    List<Prelevement> findByMontant(Double montant);

    List<Prelevement> findByDateOperationAndMontant(LocalDate dateOperation, Double montant);
}