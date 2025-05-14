package tn.spring.stationsync.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.stationsync.Entities.Prelevement;


public interface PrelevementRepository extends JpaRepository<Prelevement, Integer> {
}
