package tn.spring.stationsync.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.PrelevementRepository;
import tn.spring.stationsync.Repositories.ShellRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrelevementServiceImpl implements IPrelevementService {

    @Autowired
    private PrelevementRepository prelevementRepository;

    @Autowired
    private ShellRepository shellRepository;

    @Override
    public Prelevement savePrelevement(Prelevement prelevement) {
        return prelevementRepository.save(prelevement);
    }

    public PrelevementDetailsResponse simulateAutoAssignement(double montant, LocalDate dateOperation) {
        List<Shell> candidats = shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOperation)
                .stream()
                .filter(shell -> {
                    NatureOperation nature = shell.getNatureOperation();
                    return nature == NatureOperation.AVOIR ||
                            nature.name().startsWith("FACTURE") ||
                            nature == NatureOperation.LOYER;
                })
                .sorted(Comparator.comparing(Shell::getDatePrelevement).reversed())
                .collect(Collectors.toList());

        List<Shell> solution = trouverCombinaisonExacte(candidats, montant);

        Prelevement virtuel = new Prelevement();
        virtuel.setDateOperation(dateOperation);
        virtuel.setMontant(montant);

        return new PrelevementDetailsResponse(virtuel, solution);
    }

    public PrelevementDetailsResponse assignShellsManually(Integer prelevementId, List<Integer> shellIds) {
        Prelevement prelevement = prelevementRepository.findById(prelevementId)
                .orElseThrow(() -> new RuntimeException("Prélèvement non trouvé"));

        List<Shell> shells = shellRepository.findAllById(shellIds).stream()
                .filter(s -> s.getStatut() == Statut.EN_ATTENTE)
                .peek(s -> {
                    s.setPrelevement(prelevement);
                    s.setStatut(Statut.OK);
                })
                .collect(Collectors.toList());

        shellRepository.saveAll(shells);
        prelevement.setShells(shells);
        prelevementRepository.save(prelevement);

        return new PrelevementDetailsResponse(prelevement, shells);
    }

    private List<Shell> trouverCombinaisonExacte(List<Shell> candidats, double montantCible) {
        List<Shell> result = new ArrayList<>();
        trouverCombinaisonRecursive(candidats, 0, new ArrayList<>(), montantCible, result);
        return result;
    }

    private boolean trouverCombinaisonRecursive(List<Shell> shells, int index, List<Shell> courant,
                                                double montantCible, List<Shell> resultat) {
        double somme = courant.stream()
                .mapToDouble(shell -> {
                    NatureOperation nature = shell.getNatureOperation();
                    if (nature == NatureOperation.AVOIR) return -shell.getMontant();
                    if (nature.name().startsWith("FACTURE") || nature == NatureOperation.LOYER)
                        return shell.getMontant();
                    return 0;
                })
                .sum();

        if (Math.abs(somme - montantCible) < 0.001) {
            resultat.addAll(courant);
            return true;
        }

        if (index >= shells.size()) return false;

        for (int i = index; i < shells.size(); i++) {
            List<Shell> next = new ArrayList<>(courant);
            next.add(shells.get(i));
            if (trouverCombinaisonRecursive(shells, i + 1, next, montantCible, resultat)) return true;
        }

        return false;
    }

    @Override
    public List<Prelevement> getAllPrelevements() {
        return prelevementRepository.findAll();
    }

    @Override
    public Prelevement getPrelevement(Integer idPrelevement) {
        return prelevementRepository.findById(idPrelevement)
                .orElseThrow(() -> new RuntimeException("Prélèvement non trouvé"));
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
        Prelevement p = getPrelevement(id);
        List<Shell> utiles = p.getShells().stream()
                .filter(s -> s.getNatureOperation() != null)
                .collect(Collectors.toList());
        return new PrelevementDetailsResponse(p, utiles);
    }


}
