package tn.spring.stationsync.Services;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import tn.spring.stationsync.Dtos.MonthlyShellStats;
import tn.spring.stationsync.Dtos.ShellSearchCriteria;
import tn.spring.stationsync.Dtos.StatPerNature;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.ShellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShellServiceImpl implements IShellService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ShellRepository shellRepository;

    @Autowired
    private NotificationService notificationService;


    @Override
    public Shell saveShell(Shell shell) {

        if (shell.getNatureOperation() == NatureOperation.FACTURE_LUBRIFIANT) {
            shell.setStation(Station.ZAHRA); // on force
        }

        // Si une combinaison illogique a été envoyée (ex: FACTURE_LUBRIFIANT + BOUMHAL), on bloque
        if (shell.getNatureOperation() == NatureOperation.FACTURE_LUBRIFIANT
                && shell.getStation() != Station.ZAHRA) {
            throw new IllegalArgumentException("La nature FACTURE_LUBRIFIANT est uniquement disponible pour la station ZAHRA.");
        }

        shell.calculateDatePrelevement();
        LocalDate today = LocalDate.now();

        if (!shell.getDatePrelevement().isAfter(today)) {
            shell.setStatut(Statut.EN_ATTENTE);
        } else {
            shell.setStatut(Statut.VIDE);
        }
        return shellRepository.save(shell);
    }


    @Override

    public List<Shell> getAllShells() {
        return shellRepository.findAllOrderedByIdDesc();
    }


    @Override
    public Shell getShell(Integer idShell) {
        return shellRepository.findById(idShell).get();
    }


    @Override
    public void deleteShell(Integer idShell) {
        shellRepository.deleteById(idShell);

    }

    @Override
    public Shell updateShell(Shell updatedShell) {
        // 🔍 Récupérer l'entité existante dans la base
        Shell existingShell = shellRepository.findById(updatedShell.getIdShell())
                .orElseThrow(() -> new IllegalArgumentException("Shell not found"));

        // 🛠️ Mettre à jour les champs modifiables
        existingShell.setDateOperation(updatedShell.getDateOperation());
        existingShell.setNatureOperation(updatedShell.getNatureOperation());
        existingShell.setNumeroFacture(updatedShell.getNumeroFacture());
        existingShell.setMontant(updatedShell.getMontant());
        existingShell.setStation(updatedShell.getStation());

        // 🧮 Recalcul automatique de la date de prélèvement
        existingShell.calculateDatePrelevement();

        // 🔁 Mise à jour automatique du statut en fonction de la nouvelle date de prélèvement
        LocalDate today = LocalDate.now();
        if (!existingShell.getDatePrelevement().isAfter(today)) {
            existingShell.setStatut(Statut.EN_ATTENTE);
        } else {
            existingShell.setStatut(Statut.VIDE);
        }

        // 💾 Enregistrement
        Shell saved = shellRepository.save(existingShell);

        // Auto-resolve when status becomes OK
        if (saved.getStatut() == Statut.OK) {
            notificationService.resolveByRef(tn.spring.stationsync.Entities.NotificationType.SHELL, saved.getIdShell());
        }
        return saved;
    }


    @PostConstruct
    public void runOnStartup() {
        updateStatutsWhenPrelevementDue();
    }


    @Override
    public void updateStatutsWhenPrelevementDue() {
        LocalDate today = LocalDate.now();
        List<Shell> allShells = shellRepository.findAll();

        List<Shell> modifiedShells = new ArrayList<>();

        for (Shell shell : allShells) {
            if (shell.getDatePrelevement() == null) continue;
            if (shell.getStatut() == Statut.OK) continue;

            Statut newStatut = shell.getDatePrelevement().isAfter(today)
                    ? Statut.VIDE
                    : Statut.EN_ATTENTE;

            if (shell.getStatut() != newStatut) {
                shell.setStatut(newStatut);
                modifiedShells.add(shell); // on ne sauvegarde que ceux qui changent
            }
        }

        shellRepository.saveAll(modifiedShells); // sauvegarde groupée ➤ meilleure perf
    }

    @Override
    public List<Shell> getShellsByStatut(Statut statut) {
        return shellRepository.findByStatut(statut);
    }

    @Override
    public List<Shell> filterShells(NatureOperation nature, Station station, List<Statut> statuts) {
        System.out.println("🎯 FILTRAGE REÇU =>");
        System.out.println(" - nature = " + nature);
        System.out.println(" - station = " + station);
        System.out.println(" - statuts = " + statuts);
        return shellRepository.findByFilters(nature, station, statuts);
    }

    @Override
    public List<Shell> searchShells(ShellSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Shell> query = cb.createQuery(Shell.class);
        Root<Shell> root = query.from(Shell.class);

        List<Predicate> predicates = new ArrayList<>();

        // Date exacte
        if (criteria.getExactDateOperation() != null) {
            predicates.add(cb.equal(root.get("dateOperation"), criteria.getExactDateOperation()));
        }

        // Intervalle de dates
        if (criteria.getStartDateOperation() != null && criteria.getEndDateOperation() != null) {
            predicates.add(cb.between(root.get("dateOperation"), criteria.getStartDateOperation(), criteria.getEndDateOperation()));
        }

        // Montant exact
        if (criteria.getExactMontant() != null) {
            predicates.add(cb.equal(root.get("montant"), criteria.getExactMontant()));
        }

        // Intervalle de montants
        if (criteria.getMinMontant() != null && criteria.getMaxMontant() != null) {
            predicates.add(cb.between(root.get("montant"), criteria.getMinMontant(), criteria.getMaxMontant()));
        }

        // Natures
        if (criteria.getNatures() != null && !criteria.getNatures().isEmpty()) {
            predicates.add(root.get("natureOperation").in(criteria.getNatures()));
        }

        // Statuts
        if (criteria.getStatuts() != null && !criteria.getStatuts().isEmpty()) {
            predicates.add(root.get("statut").in(criteria.getStatuts()));
        }

        // Stations
        if (criteria.getStations() != null && !criteria.getStations().isEmpty()) {
            predicates.add(root.get("station").in(criteria.getStations()));
        }

        // NumFacture
        if (criteria.getNumeroFacture() != null && !criteria.getNumeroFacture().isEmpty()) {
            predicates.add(cb.equal(root.get("numeroFacture"), criteria.getNumeroFacture()));
        }


        //datePrelevement
        if (criteria.getExactDatePrelevement() != null) {
            predicates.add(cb.equal(root.get("datePrelevement"), criteria.getExactDatePrelevement()));
        }

        if (criteria.getStartDatePrelevement() != null && criteria.getEndDatePrelevement() != null) {
            predicates.add(cb.between(
                    root.get("datePrelevement"),
                    criteria.getStartDatePrelevement(),
                    criteria.getEndDatePrelevement()
            ));
        }



        query.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(root.get("idShell")));

        return entityManager.createQuery(query).getResultList();

    }

    @Override
    public List<MonthlyShellStats> getShellsGroupedByMonth(Optional<String> natureFilter) {
        List<Shell> shells = natureFilter.isPresent()
                ? shellRepository.findByNatureOperation(NatureOperation.valueOf(natureFilter.get())) // ✅ filtre appliqué
                : shellRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

        Map<String, List<Shell>> groupedByMonth = shells.stream()
                .collect(Collectors.groupingBy(
                        shell -> shell.getDateOperation().format(formatter)
                ));

        List<MonthlyShellStats> stats = new ArrayList<>();

        for (Map.Entry<String, List<Shell>> entry : groupedByMonth.entrySet()) {
            String mois = entry.getKey();
            List<Shell> shellListForMonth = entry.getValue();

            // ✅ Soustraire les avoirs du total
            double totalMontant = shellListForMonth.stream()
                    .mapToDouble(shell -> {
                        if (shell.getNatureOperation() == NatureOperation.AVOIR) {
                            return -shell.getMontant(); // ✅ inverser le montant des avoirs
                        } else {
                            return shell.getMontant();
                        }
                    })
                    .sum();

            int totalCount = shellListForMonth.size();

            // ✅ Garder les montants positifs dans les détails pour le graphique
            Map<String, StatPerNature> details = shellListForMonth.stream()
                    .collect(Collectors.groupingBy(
                            shell -> shell.getNatureOperation().toString(),
                            Collectors.collectingAndThen(Collectors.toList(), list -> {
                                int count = list.size();
                                double montant = list.stream().mapToDouble(Shell::getMontant).sum();
                                return new StatPerNature(count, montant);
                            })
                    ));

            stats.add(new MonthlyShellStats(mois, totalMontant, totalCount, details));
        }

        return stats;
    }





}


