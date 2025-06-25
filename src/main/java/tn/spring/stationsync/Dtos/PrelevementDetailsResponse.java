package tn.spring.stationsync.Dtos;

import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.NatureOperation;

import java.util.List;

public class PrelevementDetailsResponse {

    private Prelevement prelevement;
    private List<Shell> shells;
    private double totalFactures;
    private double totalAvoirs;
    private double montantTotalAffecte;
    private double montantRestant;
    private int nombreShells;
    private boolean exactMatch;

    public PrelevementDetailsResponse(Prelevement prelevement, List<Shell> shells) {
        this.prelevement = prelevement;
        this.shells = shells;
        this.nombreShells = shells.size();

        this.totalFactures = 0;
        this.totalAvoirs = 0;

        for (Shell shell : shells) {
            if (shell.getNatureOperation() == NatureOperation.AVOIR) {
                // On suppose que les avoirs sont positifs en base
                totalAvoirs += shell.getMontant();
            } else {
                totalFactures += shell.getMontant();
            }
        }

        this.montantTotalAffecte = totalFactures - totalAvoirs;
        this.montantRestant = prelevement.getMontant() - montantTotalAffecte;
        this.exactMatch = Math.abs(this.montantRestant) < 0.001; // Sécurité double
    }

    public Prelevement getPrelevement() {
        return prelevement;
    }

    public List<Shell> getShells() {
        return shells;
    }

    public double getTotalFactures() {
        return totalFactures;
    }

    public double getTotalAvoirs() {
        return totalAvoirs;
    }

    public double getMontantTotalAffecte() {
        return montantTotalAffecte;
    }

    public double getMontantRestant() {
        return montantRestant;
    }

    public int getNombreShells() {
        return nombreShells;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }
}
