package tn.spring.stationsync.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;


import java.util.List;


@Repository
public interface ShellRepository extends JpaRepository<Shell, Integer> {

    @Query("SELECT s FROM Shell s ORDER BY s.idShell DESC")
    List<Shell> findAllOrderedByIdDesc();

    List<Shell> findByStatut(Statut statut);

    @Query("SELECT s FROM Shell s WHERE "
            + "(:nature IS NULL OR s.natureOperation = :nature) AND "
            + "(:station IS NULL OR s.station = :station) AND "
            + "(:#{#statuts == null || #statuts.isEmpty()} = true OR s.statut IN :statuts)")
    List<Shell> findByFilters(
            @Param("nature") NatureOperation nature,
            @Param("station") Station station,
            @Param("statuts") List<Statut> statuts
    );
    List<Shell> findByStatutAndDatePrelevementBefore(Statut statut, LocalDate date);

}



