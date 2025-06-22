package tn.spring.stationsync.Dtos;

import lombok.Getter;
import lombok.Setter;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter

public class ShellSearchCriteria {

    private List<NatureOperation> natures;
    private List<Station> stations;
    private List<Statut> statuts;

    private LocalDate exactDateOperation;
    private LocalDate startDateOperation;
    private LocalDate endDateOperation;

    private LocalDate exactDatePrelevement;
    private LocalDate startDatePrelevement;
    private LocalDate endDatePrelevement;

    private Double exactMontant;
    private Double minMontant;
    private Double maxMontant;

    private String numeroFacture;

}
