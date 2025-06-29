package tn.spring.stationsync.Dtos;

import java.util.Map;

public class MonthlyShellStats {
    private String mois;
    private double totalMontant;
    private int totalCount;
    private Map<String, StatPerNature> details;

    public MonthlyShellStats(String mois, double totalMontant, int totalCount, Map<String, StatPerNature> details) {
        this.mois = mois;
        this.totalMontant = totalMontant;
        this.totalCount = totalCount;
        this.details = details;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public double getTotalMontant() {
        return totalMontant;
    }

    public void setTotalMontant(double totalMontant) {
        this.totalMontant = totalMontant;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, StatPerNature> getDetails() {
        return details;
    }

    public void setDetails(Map<String, StatPerNature> details) {
        this.details = details;
    }

    // Getters et setters
}

