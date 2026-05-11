package tn.spring.stationsync.Dtos;

import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.NatureOperation;
import java.util.ArrayList;
import java.util.List;

public class PrelevementDetailsResponse {

    private Prelevement prelevement;
    private List<Shell> shells;
    private double totalFactures;
    private double totalAvoirs;
    private double montantTotalAffecte;
    private double montantRestant;
    private int nombreShells;

    public List<PrelevementSolutionDto> getExactSolutions() {
        return exactSolutions;
    }

    public void setExactSolutions(List<PrelevementSolutionDto> exactSolutions) {
        this.exactSolutions = exactSolutions;
    }

    private boolean exactMatch;
    private List<PrelevementSolutionDto> exactSolutions = new ArrayList<>();
    private MatchType matchType;
    private List<Shell> candidateShells = new ArrayList<>();
    private int numberOfExactSolutions;

    public PrelevementDetailsResponse(Prelevement prelevement, List<Shell> shells) {
        this.prelevement = prelevement;
        this.shells = shells != null ? shells : new ArrayList<>();
        this.nombreShells = this.shells.size();

        this.totalFactures = 0;
        this.totalAvoirs = 0;

        for (Shell shell : this.shells) {
            if (shell.getNatureOperation() == NatureOperation.AVOIR) {
                totalAvoirs += shell.getMontant();
            } else {
                totalFactures += shell.getMontant();
            }
        }

        this.montantTotalAffecte = totalFactures - totalAvoirs;
        this.montantRestant = prelevement.getMontant() - montantTotalAffecte;
        this.exactMatch = Math.abs(this.montantRestant) < 0.001;
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

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public List<Shell> getCandidateShells() {
        return candidateShells;
    }

    public void setCandidateShells(List<Shell> candidateShells) {
        this.candidateShells = candidateShells;
    }

    public int getNumberOfExactSolutions() {
        return numberOfExactSolutions;
    }

    public void setNumberOfExactSolutions(int numberOfExactSolutions) {
        this.numberOfExactSolutions = numberOfExactSolutions;
    }
}