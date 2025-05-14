package tn.spring.stationsync.Dtos;

import tn.spring.stationsync.Entities.Shell;
import java.util.List;

public class ShellFilterResponse
{
    private List<Shell> shells;
    private double totalMontant;
    private int totalCount;

    public ShellFilterResponse(List<Shell> shells) {
        this.shells = shells;
        this.totalMontant = shells.stream().mapToDouble(Shell::getMontant).sum();
        this.totalCount = shells.size();
    }

    public List<Shell> getShells() {
        return shells;
    }

    public double getTotalMontant() {
        return totalMontant;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
