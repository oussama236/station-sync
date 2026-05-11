package tn.spring.stationsync.Dtos;

import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Shell;

import java.util.ArrayList;
import java.util.List;

public class PrelevementSolutionDto {

    private List<Shell> shells = new ArrayList<>();
    private double totalFactures;
    private double totalAvoirs;
    private double montantTotal;

    public PrelevementSolutionDto() {}

    public PrelevementSolutionDto(List<Shell> shells) {
        this.shells = shells;

        for (Shell shell : shells) {
            if (shell.getNatureOperation() == NatureOperation.AVOIR) {
                totalAvoirs += shell.getMontant();
            } else {
                totalFactures += shell.getMontant();
            }
        }

        this.montantTotal = totalFactures - totalAvoirs;
    }

    public List<Shell> getShells() {
        return shells;
    }

    public void setShells(List<Shell> shells) {
        this.shells = shells;
    }

    public double getTotalFactures() {
        return totalFactures;
    }

    public double getTotalAvoirs() {
        return totalAvoirs;
    }

    public double getMontantTotal() {
        return montantTotal;
    }
}