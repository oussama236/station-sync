package tn.spring.stationsync.Services;


import tn.spring.stationsync.Entities.Shell;
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
        return shellRepository.findAll();
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
    public List<Shell> findFiltered(String category, String site, List<Statut> statuts) {
        return shellRepository.findByNatureAndStatutIn(category, site, statuts);
    }
}


