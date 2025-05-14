package tn.spring.stationsync.Entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Banque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBanque;

    @Temporal(TemporalType.DATE)
    private Date dateOperation;

    @Column(nullable = false, length = 20)
    private final String numeroCompte = "20";

    private String numeroBordereau;
    private Double montant;

    public Integer getIdBanque() {
        return idBanque;
    }

    public void setIdBanque(Integer idBanque) {
        this.idBanque = idBanque;
    }

    @Enumerated(EnumType.STRING)
    private NatureOperationBank natureOperationBank;

    @Enumerated(EnumType.STRING)
    private StatutBank statutBank;



    public Date getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(Date dateOperation) {
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

    public StatutBank getStatutBank() {
        return statutBank;
    }

    public void setStatutBank(StatutBank statutBank) {
        this.statutBank = statutBank;
    }

    // Getters and Setters
}