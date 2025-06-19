package tn.spring.stationsync.Services;


import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.ShellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.annotation.PostConstruct;


import java.time.LocalDate;
import java.util.List;

@Service
public class ShellServiceImpl implements IShellService {


    @Autowired
    private ShellRepository shellRepository;


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
    public Shell updateShell(Shell s) {
        s.calculateDatePrelevement(); // recalculate based on new operation date
        shellRepository.save(s);
        return s;
    }

    @PostConstruct
    public void runOnStartup() {
        updateStatutsWhenPrelevementDue();
    }


    @Override
    public void updateStatutsWhenPrelevementDue() {
        LocalDate today = LocalDate.now();
        List<Shell> shells = shellRepository.findByStatut(Statut.VIDE);

        for (Shell shell : shells) {
            if (!shell.getDatePrelevement().isAfter(today)) {
                shell.setStatut(Statut.EN_ATTENTE);
                shellRepository.save(shell);

            }
        }
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
}


