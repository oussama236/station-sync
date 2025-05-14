package tn.spring.stationsync.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Statut;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


@Repository
public interface ShellRepository extends JpaRepository<Shell, Integer> {

    List<Shell> findByStatut(Statut statut);

    @Query("SELECT s FROM Shell s WHERE " +
            "(:statuts IS NULL OR s.statut IN :statuts) " +
            "AND (:category IS NULL OR LOWER(CAST(s.natureOperation AS string)) LIKE LOWER(CONCAT(:category, '%'))) " +
            "AND (:site IS NULL OR LOWER(CAST(s.natureOperation AS string)) LIKE LOWER(CONCAT('%', :site)))")
    List<Shell> findByNatureAndStatutIn(
            @Param("category") String category,
            @Param("site") String site,
            @Param("statuts") List<Statut> statuts
    );


}
