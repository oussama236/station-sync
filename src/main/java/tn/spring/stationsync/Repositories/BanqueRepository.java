package tn.spring.stationsync.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.spring.stationsync.Entities.Banque;



@Repository
public interface BanqueRepository extends JpaRepository<Banque, Integer> {
}
