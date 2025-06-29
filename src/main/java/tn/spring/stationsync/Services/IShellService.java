package tn.spring.stationsync.Services;

import tn.spring.stationsync.Dtos.MonthlyShellStats;
import tn.spring.stationsync.Dtos.ShellSearchCriteria;
import tn.spring.stationsync.Entities.*;

import java.util.List;
import java.util.Optional;

public interface IShellService {

    // Create a Shell
    Shell saveShell(Shell shell);

    // Retrieve all Shells
    List<Shell> getAllShells();

    // Find a Shell by ID
    Shell getShell (Integer idShell);

    // Delete a Shell by ID
    void deleteShell(Integer idShell);

    //Update Shell by ID
    Shell updateShell (Shell  s);

    void updateStatutsWhenPrelevementDue();

    List<Shell> getShellsByStatut(Statut statut);

    List<Shell> filterShells(NatureOperation nature, Station station, List<Statut> statuts);

    List<Shell> searchShells(ShellSearchCriteria criteria);

    List<MonthlyShellStats> getShellsGroupedByMonth(Optional<String> natureFilter);

}
