package tn.spring.stationsync.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private NotificationService notificationService;

    @Override
    public Prelevement savePrelevement(Prelevement prelevement) {
        return prelevementRepository.save(prelevement);
    }

    /** ---- Simulation (unchanged) ---- */
    @Override
    public PrelevementDetailsResponse simulateAutoAssignement(double montant, LocalDate dateOperation) {
        List<Shell> candidats = shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOperation)
                .stream()
                .filter(shell -> {
                    NatureOperation nature = shell.getNatureOperation();
                    return nature == NatureOperation.AVOIR
                            || nature.name().startsWith("FACTURE")
                            || nature == NatureOperation.LOYER;
                })
                .sorted(Comparator.comparing(Shell::getDatePrelevement).reversed())
                .collect(Collectors.toList());

        List<Shell> solution = trouverCombinaisonExacte(candidats, montant);

        Prelevement virtuel = new Prelevement();
        virtuel.setDateOperation(dateOperation);
        virtuel.setMontant(montant);

        return new PrelevementDetailsResponse(virtuel, solution);
    }

    /** ---- Candidates for manual affectation (EN_ATTENTE before date) ---- */
    @Override
    public List<Shell> getShellsForManualAffectation(LocalDate dateOperation) {
        return shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOperation)
                .stream()
                .sorted(Comparator.comparing(Shell::getDatePrelevement)) // oldest first
                .collect(Collectors.toList());
    }

    /** ---- Search passthrough to repository ---- */
    @Override
    public List<Prelevement> searchPrelevements(LocalDate date, Double montant) {
        return prelevementRepository.searchPrelevements(date, montant);
    }

    /** ---- Manual assignment (guard against foreign-linked shells) ---- */
    @Override
    @Transactional
    public PrelevementDetailsResponse assignShellsManually(Integer prelevementId, List<Integer> shellIds) {
        Prelevement prelevement = prelevementRepository.findById(prelevementId)
                .orElseThrow(() -> new RuntimeException("Prélèvement non trouvé"));

        List<Shell> shells = shellRepository.findAllById(shellIds).stream()
                .filter(s ->
                        // allow EN_ATTENTE or already linked to THIS prelevement
                        s.getStatut() == Statut.EN_ATTENTE
                                || (s.getPrelevement() != null
                                && Objects.equals(s.getPrelevement().getIdPrelevement(), prelevementId))
                )
                .peek(s -> {
                    s.setPrelevement(prelevement);
                    s.setStatut(Statut.OK);
                })
                .collect(Collectors.toList());

        shellRepository.saveAll(shells);
        prelevement.setShells(shells);
        prelevementRepository.save(prelevement);

        for (Shell s : shells) {
            notificationService.resolveByRef(tn.spring.stationsync.Entities.NotificationType.SHELL, s.getIdShell());
        }

        return new PrelevementDetailsResponse(prelevement, shells);
    }

    /** ---- Combo finder (unchanged) ---- */
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

    /** ---- CRUD ---- */
    @Override
    public List<Prelevement> getAllPrelevements() {
        return prelevementRepository.findAll();
    }

    @Override
    public Prelevement getPrelevement(Integer idPrelevement) {
        return prelevementRepository.findById(idPrelevement)
                .orElseThrow(() -> new RuntimeException("Prélèvement non trouvé"));
    }

    /** ---- Delete: revert shells ---- */
    @Override
    @Transactional
    public void deletePrelevement(Integer idPrelevement) {
        Prelevement p = getPrelevement(idPrelevement);

        List<Shell> shells = Optional.ofNullable(p.getShells()).orElse(List.of())
                .stream()
                .peek(s -> {
                    s.setPrelevement(null);
                    s.setStatut(Statut.EN_ATTENTE);
                })
                .collect(Collectors.toList());

        shellRepository.saveAll(shells);
        prelevementRepository.deleteById(idPrelevement);
    }

    /** ---- Update: detach removed + attach new ---- */
    @Override
    @Transactional
    public Prelevement updatePrelevement(Prelevement p) {
        Prelevement existing = getPrelevement(p.getIdPrelevement());

        existing.setDateOperation(p.getDateOperation());
        existing.setMontant(p.getMontant());

        List<Integer> newIds = Optional.ofNullable(p.getShells()).orElse(List.of())
                .stream().map(Shell::getIdShell).collect(Collectors.toList());

        List<Shell> current = Optional.ofNullable(existing.getShells()).orElse(List.of());
        Set<Integer> currentIds = current.stream().map(Shell::getIdShell).collect(Collectors.toSet());

        List<Shell> toDetach = current.stream()
                .filter(s -> !newIds.contains(s.getIdShell()))
                .peek(s -> {
                    s.setPrelevement(null);
                    s.setStatut(Statut.EN_ATTENTE);
                }).collect(Collectors.toList());

        List<Shell> toAttach = shellRepository.findAllById(newIds).stream()
                .filter(s -> !currentIds.contains(s.getIdShell()))
                .peek(s -> {
                    s.setPrelevement(existing);
                    s.setStatut(Statut.OK);
                }).collect(Collectors.toList());

        shellRepository.saveAll(toDetach);
        shellRepository.saveAll(toAttach);

        for (Shell s : toAttach)
            notificationService.resolveByRef(tn.spring.stationsync.Entities.NotificationType.SHELL, s.getIdShell());

        existing.setShells(shellRepository.findAllById(newIds));
        return prelevementRepository.save(existing);
    }

    /** ---- Details with sorted shells ---- */
    @Override
    public PrelevementDetailsResponse getPrelevementAvecResume(Integer id) {
        Prelevement p = getPrelevement(id);
        List<Shell> utiles = Optional.ofNullable(p.getShells()).orElse(List.of())
                .stream()
                .filter(s -> s.getNatureOperation() != null)
                .sorted(Comparator.comparing(Shell::getDatePrelevement))
                .collect(Collectors.toList());
        return new PrelevementDetailsResponse(p, utiles);
    }

    /** ---- Auto-assign for edit: EN_ATTENTE ∪ already linked (exclude foreign-linked) ---- */
    @Override
    @Transactional
    public PrelevementDetailsResponse autoAssign(Integer prelevementId) {
        Prelevement p = getPrelevement(prelevementId);
        double montant = p.getMontant();
        LocalDate dateOp = p.getDateOperation();

        // EN_ATTENTE before/equal date + already linked to THIS prelevement
        List<Shell> enAttente = shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOp);
        List<Shell> alreadyLinked = Optional.ofNullable(p.getShells()).orElse(List.of());

        Map<Integer, Shell> union = new LinkedHashMap<>();
        for (Shell s : alreadyLinked) union.put(s.getIdShell(), s);
        for (Shell s : enAttente) {
            if (!s.getDatePrelevement().isAfter(dateOp)) {
                union.putIfAbsent(s.getIdShell(), s);
            }
        }

        // EXCLUDE shells linked to another prelevement
        List<Shell> candidats = union.values().stream()
                .filter(s -> s.getPrelevement() == null
                        || Objects.equals(s.getPrelevement().getIdPrelevement(), p.getIdPrelevement()))
                .filter(shell -> {
                    NatureOperation nature = shell.getNatureOperation();
                    return nature == NatureOperation.AVOIR
                            || nature.name().startsWith("FACTURE")
                            || nature == NatureOperation.LOYER;
                })
                .sorted(Comparator.comparing(Shell::getDatePrelevement).reversed())
                .collect(Collectors.toList());

        List<Shell> solution = trouverCombinaisonExacte(candidats, montant);
        if (solution.isEmpty()) {
            throw new IllegalStateException("Aucune combinaison exacte trouvée pour l’auto-affectation.");
        }

        // Detach non-selected
        Set<Integer> solutionIds = solution.stream().map(Shell::getIdShell).collect(Collectors.toSet());
        List<Shell> toDetach = Optional.ofNullable(p.getShells()).orElse(List.of()).stream()
                .filter(s -> !solutionIds.contains(s.getIdShell()))
                .peek(s -> {
                    s.setPrelevement(null);
                    s.setStatut(Statut.EN_ATTENTE);
                }).collect(Collectors.toList());

        // Attach selected
        List<Shell> toAttach = solution.stream()
                .peek(s -> {
                    s.setPrelevement(p);
                    s.setStatut(Statut.OK);
                }).collect(Collectors.toList());

        shellRepository.saveAll(toDetach);
        shellRepository.saveAll(toAttach);

        for (Shell s : toAttach)
            notificationService.resolveByRef(tn.spring.stationsync.Entities.NotificationType.SHELL, s.getIdShell());

        p.setShells(toAttach);
        prelevementRepository.save(p);

        return new PrelevementDetailsResponse(p, p.getShells());
    }

    /** ---- Candidates for EDIT modal: EN_ATTENTE ∪ already linked (exclude foreign-linked) ---- */
    @Override
    public List<Shell> getCandidatesForEdit(Integer prelevementId) {
        Prelevement p = getPrelevement(prelevementId);
        LocalDate dateOp = p.getDateOperation();

        List<Shell> enAttente = shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOp);
        List<Shell> alreadyLinked = Optional.ofNullable(p.getShells()).orElse(List.of());

        Map<Integer, Shell> union = new LinkedHashMap<>();
        for (Shell s : alreadyLinked) union.put(s.getIdShell(), s);
        for (Shell s : enAttente) union.putIfAbsent(s.getIdShell(), s);

        return union.values().stream()
                .filter(s -> s.getPrelevement() == null
                        || Objects.equals(s.getPrelevement().getIdPrelevement(), p.getIdPrelevement()))
                .sorted(Comparator.comparing(Shell::getDatePrelevement))
                .collect(Collectors.toList());
    }
}
