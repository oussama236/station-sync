package tn.spring.stationsync.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface BanqueRepository extends JpaRepository<Banque, Integer> {

    @Query("""
       SELECT b
       FROM Banque b
       WHERE (:station IS NULL OR b.station = :station)
       AND (:statuts IS NULL OR b.statut IN :statuts)
       AND (:dateFrom IS NULL OR b.dateOperation >= :dateFrom)
       AND (:dateTo IS NULL OR b.dateOperation <= :dateTo)
       ORDER BY b.dateOperation ASC
       """)
    List<Banque> findByFilters(
            Station station,
            List<Statut> statuts,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    @Query("SELECT b FROM Banque b ORDER BY b.idBanque DESC")
    List<Banque> findAllOrderedByIdDesc();

    List<Banque> findByStatutAndDateOperationLessThanEqual(Statut statut, java.time.LocalDate date);

}
