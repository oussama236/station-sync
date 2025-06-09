package tn.spring.stationsync.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.PrelevementRepository;
import tn.spring.stationsync.Repositories.ShellRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrelevementServiceImpl implements IPrelevementService {

    @Autowired
    private PrelevementRepository prelevementRepository;

    @Autowired
    private ShellRepository shellRepository;

    @Override
    public Prelevement savePrelevement(Prelevement prelevement) {
        // Étape 1 : sauvegarde du prélèvement
        Prelevement saved = prelevementRepository.save(prelevement);

        // Étape 2 : assignation automatique des Shells
        assignShellsToPrelevement(saved);

        return saved;
    }

    public Prelevement assignShellsToPrelevement(Prelevement prelevement) {
        double montantRestant = prelevement.getMontant();
        LocalDate dateLimite = prelevement.getDateOperation();

        // Récupérer les Shells en attente avec une date <= opération
        List<Shell> candidats = shellRepository.findByStatutAndDatePrelevementBefore(
                Statut.EN_ATTENTE, dateLimite
        );

        List<Shell> assignés = new ArrayList<>();

        for (Shell shell : candidats) {
            String nature = shell.getNatureOperation().name();

            if ((nature.startsWith("FACTURE") || nature.startsWith("LOYER")) &&
                    montantRestant >= shell.getMontant()) {

                shell.setPrelevement(prelevement);
                shell.setStatut(Statut.OK);
                montantRestant -= shell.getMontant();
                assignés.add(shell);
            }
        }

        prelevement.setShells(assignés);
        shellRepository.saveAll(assignés);
        return prelevementRepository.save(prelevement);
    }

    @Override
    public List<Prelevement> getAllPrelevements() {
        return prelevementRepository.findAll();
    }

    @Override
    public Prelevement getPrelevement(Integer idPrelevement) {
        return prelevementRepository.findById(idPrelevement).get();
    }

    @Override
    public void deletePrelevement(Integer idPrelevement) {
        prelevementRepository.deleteById(idPrelevement);
    }

    @Override
    public Prelevement updatePrelevement(Prelevement p) {
        return prelevementRepository.save(p);
    }

    public PrelevementDetailsResponse getPrelevementAvecResume(Integer id) {
        Prelevement p = prelevementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prélèvement non trouvé"));

        List<Shell> shellsUtiles = p.getShells().stream()
                .filter(s -> {
                    String nature = s.getNatureOperation().name();
                    return nature.startsWith("FACTURE") || nature.startsWith("LOYER");
                }).toList();

        return new PrelevementDetailsResponse(p, shellsUtiles);
    }
}