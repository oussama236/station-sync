package tn.spring.stationsync.Specifications;

import org.springframework.data.jpa.domain.Specification;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;

import java.time.LocalDate;
import java.util.List;

public class BanqueSpecification {

    public static Specification<Banque> hasStation(Station station) {
        return (root, query, criteriaBuilder) ->
                station == null ? null : criteriaBuilder.equal(root.get("station"), station);
    }

    public static Specification<Banque> hasStatuts(List<Statut> statuts) {
        return (root, query, criteriaBuilder) -> {
            if (statuts == null || statuts.isEmpty()) {
                return null;
            }
            return root.get("statut").in(statuts);
        };
    }

    public static Specification<Banque> dateOperationGreaterThanOrEqual(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) ->
                dateFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("dateOperation"), dateFrom);
    }

    public static Specification<Banque> dateOperationLessThanOrEqual(LocalDate dateTo) {
        return (root, query, criteriaBuilder) ->
                dateTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("dateOperation"), dateTo);
    }
}