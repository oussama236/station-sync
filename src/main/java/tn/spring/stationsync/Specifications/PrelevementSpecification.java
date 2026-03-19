package tn.spring.stationsync.Specifications;

import org.springframework.data.jpa.domain.Specification;
import tn.spring.stationsync.Entities.Prelevement;

import java.time.LocalDate;

public class PrelevementSpecification {

    public static Specification<Prelevement> dateOperationGreaterThanOrEqual(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) ->
                dateFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("dateOperation"), dateFrom);
    }

    public static Specification<Prelevement> dateOperationLessThanOrEqual(LocalDate dateTo) {
        return (root, query, criteriaBuilder) ->
                dateTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("dateOperation"), dateTo);
    }
}