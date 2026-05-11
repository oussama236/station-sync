package tn.spring.stationsync.Services;

import tn.spring.stationsync.Dtos.PrelevementSolutionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.stationsync.Dtos.MatchType;
import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.NotificationType;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.PrelevementRepository;
import tn.spring.stationsync.Repositories.ShellRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tn.spring.stationsync.Specifications.PrelevementSpecification;
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

    // =========================================================================
    // SAVE
    // =========================================================================

    @Override
    public Prelevement savePrelevement(Prelevement prelevement) {
        return prelevementRepository.save(prelevement);
    }

    // =========================================================================
    // SIMULATION (no persistence)
    // =========================================================================


    @Override
    public PrelevementDetailsResponse simulateAutoAssignement(double montant, LocalDate dateOperation) {
        List<Shell> candidats = buildCandidats(
                shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOperation),
                null,
                dateOperation
        );

        MatchingAnalysisResult analysis = analyzeMatching(candidats, montant);

        Prelevement virtuel = new Prelevement();
        virtuel.setDateOperation(dateOperation);
        virtuel.setMontant(montant);

        PrelevementDetailsResponse response = new PrelevementDetailsResponse(virtuel, analysis.autoAssignedShells);
        response.setMatchType(analysis.matchType);
        response.setCandidateShells(analysis.candidateShells);
        response.setExactSolutions(toSolutionDtos(analysis.exactSolutions));
        response.setNumberOfExactSolutions(analysis.numberOfExactSolutions);
        response.setExactMatch(analysis.matchType == MatchType.UNIQUE_MATCH);

        return response;
    }
    // =========================================================================
    // CANDIDATES FOR MANUAL AFFECTATION (new prelevement)
    // =========================================================================

    @Override
    public List<Shell> getShellsForManualAffectation(LocalDate dateOperation) {
        return shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOperation)
                .stream()
                .sorted(Comparator.comparing(Shell::getDatePrelevement))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    public List<Prelevement> searchPrelevements(LocalDate date, Double montant) {

        if (date != null && montant != null) {
            return prelevementRepository.findByDateOperationAndMontant(date, montant);
        }

        if (date != null) {
            return prelevementRepository.findByDateOperation(date);
        }

        if (montant != null) {
            return prelevementRepository.findByMontant(montant);
        }

        return prelevementRepository.findAll();
    }
    // =========================================================================
    // MANUAL ASSIGNMENT
    // =========================================================================

    @Override
    @Transactional
    public PrelevementDetailsResponse assignShellsManually(Integer prelevementId, List<Integer> shellIds) {
        Prelevement prelevement = findPrelevementOrThrow(prelevementId);

        List<Shell> shells = shellRepository.findAllById(shellIds).stream()
                .filter(s -> isEligibleForAssignment(s, prelevementId))
                .collect(Collectors.toList());

        attachShells(shells, prelevement);
        shellRepository.saveAll(shells);

        prelevement.setShells(shells);
        prelevementRepository.save(prelevement);

        resolveNotifications(shells);

        return new PrelevementDetailsResponse(prelevement, shells);
    }

    // =========================================================================
    // AUTO-ASSIGN (for existing prelevement)
    // =========================================================================

    @Override
    @Transactional
    public PrelevementDetailsResponse autoAssign(Integer prelevementId) {
        Prelevement p = findPrelevementOrThrow(prelevementId);

        List<Shell> candidats = buildCandidats(
                shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, p.getDateOperation()),
                p,
                p.getDateOperation()
        );

        MatchingAnalysisResult analysis = analyzeMatching(candidats, p.getMontant());

        if (analysis.matchType == MatchType.UNIQUE_MATCH) {
            applyAssignment(p, analysis.autoAssignedShells);

            PrelevementDetailsResponse response = new PrelevementDetailsResponse(p, p.getShells());
            response.setMatchType(MatchType.UNIQUE_MATCH);
            response.setCandidateShells(analysis.candidateShells);
            response.setExactSolutions(toSolutionDtos(analysis.exactSolutions)); // ✅ AJOUT ICI
            response.setNumberOfExactSolutions(analysis.numberOfExactSolutions);
            response.setExactMatch(true);
            return response;
        }

        PrelevementDetailsResponse response = new PrelevementDetailsResponse(p, new ArrayList<>());
        response.setMatchType(analysis.matchType);
        response.setCandidateShells(analysis.candidateShells);
        response.setExactSolutions(toSolutionDtos(analysis.exactSolutions)); // ✅ AJOUT ICI
        response.setNumberOfExactSolutions(analysis.numberOfExactSolutions);
        response.setExactMatch(false);

        return response;
    }
    // =========================================================================
    // CANDIDATES FOR EDIT MODAL
    // =========================================================================

    @Override
    public List<Shell> getCandidatesForEdit(Integer prelevementId) {
        Prelevement p = findPrelevementOrThrow(prelevementId);

        return buildUnion(
                shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, p.getDateOperation()),
                p
        ).values().stream()
                .filter(s -> isEligibleForAssignment(s, prelevementId))
                .sorted(Comparator.comparing(Shell::getDatePrelevement))
                .collect(Collectors.toList());
    }

    @Override
    public List<Prelevement> getFilteredPrelevements(LocalDate dateFrom, LocalDate dateTo) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before or equal to dateTo");
        }

        Specification<Prelevement> specification = Specification
                .where(PrelevementSpecification.dateOperationGreaterThanOrEqual(dateFrom))
                .and(PrelevementSpecification.dateOperationLessThanOrEqual(dateTo));

        return prelevementRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "dateOperation"));
    }
    // =========================================================================
    // CRUD
    // =========================================================================

    @Override
    public List<Prelevement> getAllPrelevements() {
        return prelevementRepository.findAllByOrderByIdPrelevementDesc();
    }

    @Override
    public Prelevement getPrelevement(Integer idPrelevement) {
        return findPrelevementOrThrow(idPrelevement);
    }

    @Override
    @Transactional
    public void deletePrelevement(Integer idPrelevement) {
        Prelevement p = findPrelevementOrThrow(idPrelevement);

        List<Shell> shells = Optional.ofNullable(p.getShells()).orElse(List.of());
        detachShells(shells);
        shellRepository.saveAll(shells);

        prelevementRepository.deleteById(idPrelevement);
    }

    @Override
    @Transactional
    public Prelevement updatePrelevement(Prelevement p) {
        Prelevement existing = findPrelevementOrThrow(p.getIdPrelevement());

        existing.setDateOperation(p.getDateOperation());
        existing.setMontant(p.getMontant());

        List<Integer> newIds = Optional.ofNullable(p.getShells()).orElse(List.of())
                .stream()
                .map(Shell::getIdShell)
                .collect(Collectors.toList());

        Set<Integer> currentIds = Optional.ofNullable(existing.getShells()).orElse(List.of())
                .stream()
                .map(Shell::getIdShell)
                .collect(Collectors.toSet());

        // Detach shells removed from the list
        List<Shell> toDetach = Optional.ofNullable(existing.getShells()).orElse(List.of())
                .stream()
                .filter(s -> !newIds.contains(s.getIdShell()))
                .collect(Collectors.toList());
        detachShells(toDetach);
        shellRepository.saveAll(toDetach);

        // Attach newly added shells
        List<Shell> toAttach = shellRepository.findAllById(newIds)
                .stream()
                .filter(s -> !currentIds.contains(s.getIdShell()))
                .collect(Collectors.toList());
        attachShells(toAttach, existing);
        shellRepository.saveAll(toAttach);

        resolveNotifications(toAttach);

        existing.setShells(shellRepository.findAllById(newIds));
        return prelevementRepository.save(existing);
    }

    // =========================================================================
    // DETAILS WITH SORTED SHELLS
    // =========================================================================

    @Override
    public PrelevementDetailsResponse getPrelevementAvecResume(Integer id) {
        Prelevement p = findPrelevementOrThrow(id);

        List<Shell> utiles = Optional.ofNullable(p.getShells()).orElse(List.of())
                .stream()
                .filter(s -> s.getNatureOperation() != null)
                .sorted(Comparator.comparing(Shell::getDatePrelevement))
                .collect(Collectors.toList());

        return new PrelevementDetailsResponse(p, utiles);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /** Fetch prelevement or throw a descriptive exception. */
    private Prelevement findPrelevementOrThrow(Integer id) {
        return prelevementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prélèvement non trouvé : id=" + id));
    }

    /**
     * Build candidate list for auto-assignment:
     * EN_ATTENTE shells on or before dateOp, plus shells already linked to this prelevement.
     * Excludes shells linked to a DIFFERENT prelevement.
     * Filters to AVOIR / FACTURE* / LOYER natures only, then sorts newest-first.
     */
    private List<Shell> buildCandidats(List<Shell> enAttente, Prelevement owner, LocalDate dateOp) {
        Map<Integer, Shell> union = buildUnion(enAttente, owner);

        Integer ownerId = (owner != null) ? owner.getIdPrelevement() : null;

        return union.values().stream()
                .filter(s -> s.getPrelevement() == null
                        || Objects.equals(s.getPrelevement().getIdPrelevement(), ownerId))
                .filter(s -> {
                    NatureOperation n = s.getNatureOperation();
                    return n == NatureOperation.AVOIR
                            || n.name().startsWith("FACTURE")
                            || n == NatureOperation.LOYER;
                })
                .filter(s -> !s.getDatePrelevement().isAfter(dateOp))
                .sorted(Comparator.comparing(Shell::getDatePrelevement).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Merge EN_ATTENTE shells with shells already linked to the given prelevement,
     * deduplicating by id (linked shells take priority).
     */
    private Map<Integer, Shell> buildUnion(List<Shell> enAttente, Prelevement owner) {
        Map<Integer, Shell> union = new LinkedHashMap<>();

        if (owner != null) {
            Optional.ofNullable(owner.getShells()).orElse(List.of())
                    .forEach(s -> union.put(s.getIdShell(), s));
        }

        enAttente.forEach(s -> union.putIfAbsent(s.getIdShell(), s));
        return union;
    }

    /** True if the shell is EN_ATTENTE or already linked to the given prelevement. */
    private boolean isEligibleForAssignment(Shell s, Integer prelevementId) {
        return s.getStatut() == Statut.EN_ATTENTE
                || (s.getPrelevement() != null
                && Objects.equals(s.getPrelevement().getIdPrelevement(), prelevementId));
    }

    /** Set shell → prelevement link and mark OK. */
    private void attachShells(List<Shell> shells, Prelevement prelevement) {
        for (Shell s : shells) {
            s.setPrelevement(prelevement);
            s.setStatut(Statut.OK);
        }
    }

    /** Remove shell → prelevement link and revert to EN_ATTENTE. */
    private void detachShells(List<Shell> shells) {
        for (Shell s : shells) {
            s.setPrelevement(null);
            s.setStatut(Statut.EN_ATTENTE);
        }
    }

    /** Resolve SHELL notifications for each shell. */
    private void resolveNotifications(List<Shell> shells) {
        for (Shell s : shells) {
            notificationService.resolveByRef(NotificationType.SHELL, s.getIdShell());
        }
    }

    /**
     * Detach old shells not in solution, attach solution shells, persist, notify.
     */
    private void applyAssignment(Prelevement p, List<Shell> solution) {
        Set<Integer> solutionIds = solution.stream()
                .map(Shell::getIdShell)
                .collect(Collectors.toSet());

        List<Shell> toDetach = Optional.ofNullable(p.getShells()).orElse(List.of())
                .stream()
                .filter(s -> !solutionIds.contains(s.getIdShell()))
                .collect(Collectors.toList());

        detachShells(toDetach);
        shellRepository.saveAll(toDetach);

        attachShells(solution, p);
        shellRepository.saveAll(solution);

        resolveNotifications(solution);

        p.setShells(solution);
        prelevementRepository.save(p);
    }

    // =========================================================================
    // SUBSET-SUM — optimised backtracking with suffix-sum pruning
    // =========================================================================

    /**
     * Finds a combination of shells whose signed contributions sum exactly to
     * {@code montantCible} (±0.001 tolerance).
     *
     * <p>Algorithm: recursive backtracking with two pruning strategies:
     * <ol>
     *   <li>Suffix-sum upper-bound: if the remaining shells cannot possibly reach
     *       the target, abandon the branch immediately.</li>
     *   <li>Sorted descending by amount so large shells are tried first, making
     *       successful branches reachable faster.</li>
     * </ol>
     * Worst-case is still O(2^n) but in practice the pruning keeps it tractable
     * for the dozens of shells typically found in this domain.
     */
    private List<Shell> trouverCombinaisonExacte(List<Shell> candidats, double montantCible) {
        // Sort descending: try largest contributions first for faster convergence
        List<Shell> sorted = candidats.stream()
                .sorted(Comparator.comparingDouble(s -> -getContribution(s)))
                .collect(Collectors.toList());

        // Precompute suffix sums (absolute contributions) for upper-bound pruning
        int n = sorted.size();
        double[] suffixSum = new double[n + 1];
        for (int i = n - 1; i >= 0; i--) {
            suffixSum[i] = suffixSum[i + 1] + Math.abs(getContribution(sorted.get(i)));
        }

        List<Shell> result = new ArrayList<>();
        trouverCombinaisonRecursive(sorted, 0, new ArrayList<>(), 0.0, montantCible, result, suffixSum);
        return result;
    }

    private boolean trouverCombinaisonRecursive(
            List<Shell> shells,
            int index,
            List<Shell> courant,
            double sommeCourante,
            double cible,
            List<Shell> resultat,
            double[] suffixSum) {

        // Base case: exact match found
        if (Math.abs(sommeCourante - cible) < 0.001) {
            resultat.addAll(courant);
            return true;
        }

        // Pruning: no more shells, or remaining shells cannot bridge the gap
        if (index >= shells.size()) return false;
        double remaining = cible - sommeCourante;
        if (Math.abs(remaining) > suffixSum[index] + 0.001) return false;

        for (int i = index; i < shells.size(); i++) {
            Shell s = shells.get(i);
            courant.add(s);
            if (trouverCombinaisonRecursive(
                    shells, i + 1, courant,
                    sommeCourante + getContribution(s),
                    cible, resultat, suffixSum)) {
                return true;
            }
            courant.remove(courant.size() - 1);
        }

        return false;
    }

    /**
     * Returns the signed monetary contribution of a shell:
     * AVOIR → negative (credit reduces the debit),
     * FACTURE* / LOYER → positive (charges increase the debit),
     * anything else → 0.
     */
    private double getContribution(Shell shell) {
        NatureOperation nature = shell.getNatureOperation();
        if (nature == NatureOperation.AVOIR) return -shell.getMontant();
        if (nature.name().startsWith("FACTURE") || nature == NatureOperation.LOYER) return shell.getMontant();
        return 0.0;
    }

    private static class MatchingAnalysisResult {
        private final MatchType matchType;
        private final List<Shell> autoAssignedShells;
        private final List<Shell> candidateShells;
        private final List<List<Shell>> exactSolutions;
        private final int numberOfExactSolutions;

        public MatchingAnalysisResult(
                MatchType matchType,
                List<Shell> autoAssignedShells,
                List<Shell> candidateShells,
                List<List<Shell>> exactSolutions,
                int numberOfExactSolutions
        ) {
            this.matchType = matchType;
            this.autoAssignedShells = autoAssignedShells;
            this.candidateShells = candidateShells;
            this.exactSolutions = exactSolutions;
            this.numberOfExactSolutions = numberOfExactSolutions;
        }
    }

    private MatchingAnalysisResult analyzeMatching(List<Shell> candidats, double montantCible) {

        List<List<Shell>> exactSolutions = trouverToutesLesCombinaisonsExactes(candidats, montantCible);

        if (exactSolutions.size() == 1) {
            List<Shell> uniqueSolution = exactSolutions.get(0);

            return new MatchingAnalysisResult(
                    MatchType.UNIQUE_MATCH,
                    uniqueSolution,
                    uniqueSolution,
                    exactSolutions,
                    1
            );
        }

        if (exactSolutions.size() > 1) {

            List<Shell> candidateShells = exactSolutions.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(
                            Shell::getIdShell,
                            s -> s,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(Shell::getDatePrelevement))
                    .collect(Collectors.toList());

            return new MatchingAnalysisResult(
                    MatchType.AMBIGUOUS_MATCH,
                    new ArrayList<>(),
                    candidateShells,
                    exactSolutions,
                    exactSolutions.size()
            );
        }

        List<Shell> allCandidates = candidats.stream()
                .sorted(Comparator.comparing(Shell::getDatePrelevement))
                .collect(Collectors.toList());

        return new MatchingAnalysisResult(
                MatchType.NO_EXACT_MATCH,
                new ArrayList<>(),
                allCandidates,
                exactSolutions,
                0
        );
    }
    private List<List<Shell>> trouverToutesLesCombinaisonsExactes(List<Shell> candidats, double montantCible) {
        List<Shell> sorted = candidats.stream()
                .sorted(Comparator.comparingDouble((Shell s) -> Math.abs(getContribution(s))).reversed())
                .collect(Collectors.toList());

        List<List<Shell>> resultats = new ArrayList<>();
        trouverToutesLesCombinaisonsRecursif(sorted, 0, new ArrayList<>(), 0.0, montantCible, resultats);

        return resultats;
    }

    private void trouverToutesLesCombinaisonsRecursif(
            List<Shell> shells,
            int index,
            List<Shell> courant,
            double sommeCourante,
            double cible,
            List<List<Shell>> resultats
    ) {
        if (Math.abs(sommeCourante - cible) < 0.001) {
            resultats.add(new ArrayList<>(courant));
            return;
        }

        if (index >= shells.size()) {
            return;
        }

        for (int i = index; i < shells.size(); i++) {
            Shell shell = shells.get(i);
            courant.add(shell);

            trouverToutesLesCombinaisonsRecursif(
                    shells,
                    i + 1,
                    courant,
                    sommeCourante + getContribution(shell),
                    cible,
                    resultats
            );

            courant.remove(courant.size() - 1);
        }
    }

    private List<PrelevementSolutionDto> toSolutionDtos(List<List<Shell>> exactSolutions) {
        return exactSolutions.stream()
                .map(solution -> solution.stream()
                        .sorted(Comparator.comparing(Shell::getDatePrelevement))
                        .collect(Collectors.toList()))
                .map(PrelevementSolutionDto::new)
                .collect(Collectors.toList());
    }
}