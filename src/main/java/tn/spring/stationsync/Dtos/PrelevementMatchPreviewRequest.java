package tn.spring.stationsync.Dtos;

import java.time.LocalDate;

public class PrelevementMatchPreviewRequest {

    private double montant;
    private LocalDate dateOperation;

    // Constructeur par défaut (obligatoire pour Spring)
    public PrelevementMatchPreviewRequest() {
    }

    // Constructeur avec paramètres (optionnel mais utile)
    public PrelevementMatchPreviewRequest(double montant, LocalDate dateOperation) {
        this.montant = montant;
        this.dateOperation = dateOperation;
    }

    // Getters & Setters
    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }
}


