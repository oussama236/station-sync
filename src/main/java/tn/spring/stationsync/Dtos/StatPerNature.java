package tn.spring.stationsync.Dtos;

public class StatPerNature {
    private int count;
    private double montant;

    public StatPerNature(int count, double montant) {
        this.count = count;
        this.montant = montant;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    // Getters et setters
}
