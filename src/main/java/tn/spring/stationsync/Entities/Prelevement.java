package tn.spring.stationsync.Entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
public class Prelevement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPrelevement;

    private LocalDate dateOperation;

    @Column(nullable = false)
    private final Integer numeroCompte = 20;

    @Column(nullable = false)
    private Double montant;

    public Integer getIdPrelevement() {
        return idPrelevement;
    }

    public void setIdPrelevement(Integer idPrelevement) {
        this.idPrelevement = idPrelevement;
    }


    public Integer getNumeroCompte() {
        return numeroCompte;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    public List<Shell> getShells() {
        return shells;
    }

    public void setShells(List<Shell> shells) {
        this.shells = shells;
    }

    @OneToMany(mappedBy = "prelevement")
    private List<Shell> shells;
}
