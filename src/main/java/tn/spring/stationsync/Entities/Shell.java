package tn.spring.stationsync.Entities;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
public class Shell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idShell;

    public Integer getIdShell() {
        return idShell;
    }

    public void setIdShell(Integer idShell) {
        this.idShell = idShell;
    }

    @Column(name = "date_operation")
    private LocalDate dateOperation;


    @Enumerated(EnumType.STRING)
    @Column(name = "nature_operation")
    private NatureOperation natureOperation;

    private String numeroFacture;

    private Double montant;

    @Column(name = "date_prelevement")
    private LocalDate datePrelevement;


    @Enumerated(EnumType.STRING)
    private Statut statut;

    // Getters and Setters


    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    public LocalDate getDatePrelevement() {
        return datePrelevement;
    }

    public void setDatePrelevement(LocalDate datePrelevement) {
        this.datePrelevement = datePrelevement;
    }

    public NatureOperation getNatureOperation() {
        return natureOperation;
    }

    public void setNatureOperation(NatureOperation natureOperation) {
        this.natureOperation = natureOperation;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }


    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }


    public void calculateDatePrelevement() {
        LocalDate date = this.dateOperation;

        switch (this.natureOperation) {
            case AVOIR_BOUMHAL:
            case AVOIR_ZAHRA:
                date = date.plusDays(1);
                break;

            case FACTURE_CARBURANT_BOUMHAL:
            case FACTURE_CARBURANT_ZAHRA:
                date = date.plusDays(3);
                break;

            case FACTURE_LUBRIFIANT:
                // Move to the last day of the next month
                date = date.plusMonths(1).withDayOfMonth(date.plusMonths(1).lengthOfMonth());
                break;

            case LOYER_BOUMHAL:
            case LOYER_ZAHRA:
                // Move to the last day of the current month
                date = date.withDayOfMonth(date.lengthOfMonth());
                break;
        }

        this.datePrelevement = date;
    }

}