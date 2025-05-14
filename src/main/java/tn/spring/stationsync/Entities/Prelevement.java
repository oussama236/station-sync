package tn.spring.stationsync.Entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Prelevement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPrelevement;

    @Temporal(TemporalType.DATE)
    private Date dateOperation;

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

    public Date getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(Date dateOperation) {
        this.dateOperation = dateOperation;
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
}
