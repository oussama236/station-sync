package tn.spring.stationsync.Entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Banque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBanque;

    @Column(name = "date_operation")
    private LocalDate dateOperation;

    @Column(nullable = false, length = 20)
    private final String numeroCompte = "20";

    private String numeroBordereau;
    private Double montant;
    @Enumerated(EnumType.STRING)
    private NatureOperationBank natureOperationBank;
    @Enumerated(EnumType.STRING)
    private Statut statut;
    @Enumerated(EnumType.STRING)
    private Station station;

    public Integer getIdBanque() {
        return idBanque;
    }

    public void setIdBanque(Integer idBanque) {
        this.idBanque = idBanque;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public String getNumeroBordereau() {
        return numeroBordereau;
    }

    public void setNumeroBordereau(String numeroBordereau) {
        this.numeroBordereau = numeroBordereau;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public NatureOperationBank getNatureOperationBank() {
        return natureOperationBank;
    }

    public void setNatureOperationBank(NatureOperationBank natureOperationBank) {
        this.natureOperationBank = natureOperationBank;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

// Getters and Setters
}