package tn.spring.stationsync.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.ShellRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShellServiceImplTest {

    @InjectMocks
    private ShellServiceImpl shellServiceImpl;

    @Mock
    private ShellRepository shellRepository;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- TEST 1 : saveShell -> datePrelevement + statut EN_ATTENTE ----------------
    @Test
    void saveShell_factureCarburant_setsDatePrelevementAndEnAttente() {
        LocalDate today = LocalDate.now();

        Shell shell = new Shell();
        shell.setDateOperation(today.minusDays(3)); // +3 jours => aujourd'hui
        shell.setNatureOperation(NatureOperation.FACTURE_CARBURANT);
        shell.setStation(Station.BOUMHAL);

        when(shellRepository.save(any(Shell.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shell result = shellServiceImpl.saveShell(shell);

        assertEquals(today, result.getDatePrelevement(), "datePrelevement doit être aujourd'hui");
        assertEquals(Statut.EN_ATTENTE, result.getStatut(), "Statut doit être EN_ATTENTE");
        verify(shellRepository, times(1)).save(any(Shell.class));
    }

    // ---------------- TEST 2 : saveShell -> statut VIDE si datePrelevement > today ----------------
    @Test
    void saveShell_avoir_futurePrelevement_setsStatutVide() {
        LocalDate today = LocalDate.now();

        Shell shell = new Shell();
        shell.setDateOperation(today); // AVOIR => +1 jour => > today
        shell.setNatureOperation(NatureOperation.AVOIR);
        shell.setStation(Station.ZAHRA);

        when(shellRepository.save(any(Shell.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shell result = shellServiceImpl.saveShell(shell);

        assertTrue(result.getDatePrelevement().isAfter(today), "datePrelevement doit être > today");
        assertEquals(Statut.VIDE, result.getStatut(), "Statut doit être VIDE");
        verify(shellRepository, times(1)).save(any(Shell.class));
    }

    // ---------------- TEST 3 : updateStatutsWhenPrelevementDue ----------------
    @Test
    void updateStatutsWhenPrelevementDue_updatesOnlyOverdueNonOkShells() {
        LocalDate today = LocalDate.now();

        Shell overdue = new Shell();
        overdue.setDatePrelevement(today.minusDays(1));
        overdue.setStatut(Statut.VIDE);

        Shell future = new Shell();
        future.setDatePrelevement(today.plusDays(2));
        future.setStatut(Statut.VIDE);

        Shell alreadyOk = new Shell();
        alreadyOk.setDatePrelevement(today.minusDays(5));
        alreadyOk.setStatut(Statut.OK);

        List<Shell> shells = Arrays.asList(overdue, future, alreadyOk);

        when(shellRepository.findAll()).thenReturn(shells);

        shellServiceImpl.updateStatutsWhenPrelevementDue();

        // overdue doit passer à EN_ATTENTE
        assertEquals(Statut.EN_ATTENTE, overdue.getStatut(), "Overdue doit être EN_ATTENTE");
        // future reste VIDE
        assertEquals(Statut.VIDE, future.getStatut(), "Future doit rester VIDE");
        // OK ne change pas
        assertEquals(Statut.OK, alreadyOk.getStatut(), "OK doit rester OK");

        verify(shellRepository, times(1)).findAll();
        verify(shellRepository, times(1)).saveAll(anyList());
    }
}
