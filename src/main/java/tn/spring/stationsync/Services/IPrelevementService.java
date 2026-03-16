package tn.spring.stationsync.Services;

import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;

import java.time.LocalDate;
import java.util.List;

public interface IPrelevementService {

    Prelevement savePrelevement(Prelevement prelevement);

    List<Prelevement> getAllPrelevements();

    Prelevement getPrelevement(Integer idPrelevement);

    void deletePrelevement(Integer idPrelevement);

    Prelevement updatePrelevement(Prelevement p);

    PrelevementDetailsResponse getPrelevementAvecResume(Integer id);

    PrelevementDetailsResponse assignShellsManually(Integer prelevementId, List<Integer> shellIds);

    PrelevementDetailsResponse simulateAutoAssignement(double montant, LocalDate dateOperation);

    List<Shell> getShellsForManualAffectation(LocalDate dateOperation);

    List<Prelevement> searchPrelevements(LocalDate date, Double montant);

    /** 🔹 NEW: Auto-assign shells when editing a Prelevement */
    PrelevementDetailsResponse autoAssign(Integer prelevementId);

    /** 🔹 NEW: Return candidates (EN_ATTENTE ∪ already linked) for edit modal */
    List<Shell> getCandidatesForEdit(Integer prelevementId);

    List<Prelevement> getFilteredPrelevements(LocalDate dateFrom, LocalDate dateTo);
}
