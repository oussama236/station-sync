package tn.spring.stationsync.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Repositories.PrelevementRepository;
import tn.spring.stationsync.Repositories.ShellRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PrelevementServiceImplTest {

    @InjectMocks
    private PrelevementServiceImpl prelevementService;

    @Mock
    private PrelevementRepository prelevementRepository;

    @Mock
    private ShellRepository shellRepository;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------- TEST 1 : simulateAutoAssignement -> combinaison exacte simple --------------
    @Test
    void simulateAutoAssignement_shouldReturnExactSingleShell() {
        LocalDate dateOp = LocalDate.of(2025, 1, 10);
        double montant = 100.0;

        Shell candidate = new Shell();
        candidate.setIdShell(1);
        candidate.setMontant(100.0);
        candidate.setNatureOperation(NatureOperation.FACTURE_CARBURANT);
        candidate.setDatePrelevement(LocalDate.of(2025, 1, 5));
        candidate.setStatut(Statut.EN_ATTENTE);

        when(shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOp))
                .thenReturn(List.of(candidate));

        PrelevementDetailsResponse response =
                prelevementService.simulateAutoAssignement(montant, dateOp);

        assertNotNull(response);
        assertNotNull(response.getPrelevement());
        assertEquals(montant, response.getPrelevement().getMontant());
        assertEquals(dateOp, response.getPrelevement().getDateOperation());

        List<Shell> shells = response.getShells();
        assertEquals(1, shells.size(), "Il doit y avoir 1 shell dans la solution");
        assertEquals(candidate.getIdShell(), shells.get(0).getIdShell());
    }

    // -------------- TEST 2 : getShellsForManualAffectation -> tri par datePrelevement --------------
    @Test
    void getShellsForManualAffectation_shouldReturnSortedByDatePrelevement() {
        LocalDate dateOp = LocalDate.of(2025, 1, 10);

        Shell s1 = new Shell();
        s1.setIdShell(1);
        s1.setDatePrelevement(LocalDate.of(2025, 1, 3));
        s1.setStatut(Statut.EN_ATTENTE);

        Shell s2 = new Shell();
        s2.setIdShell(2);
        s2.setDatePrelevement(LocalDate.of(2025, 1, 1));
        s2.setStatut(Statut.EN_ATTENTE);

        when(shellRepository.findByStatutAndDatePrelevementBefore(Statut.EN_ATTENTE, dateOp))
                .thenReturn(Arrays.asList(s1, s2));

        List<Shell> result = prelevementService.getShellsForManualAffectation(dateOp);

        assertEquals(2, result.size());
        // Doit être trié : plus ancien en premier (1er janvier avant 3 janvier)
        assertEquals(s2.getIdShell(), result.get(0).getIdShell());
        assertEquals(s1.getIdShell(), result.get(1).getIdShell());
    }

    // -------------- TEST 3 : assignShellsManually -> lie les shells + statut OK + notifications --------------
    @Test
    void assignShellsManually_shouldAttachShellsAndSetOkAndResolveNotifications() {
        Integer prelevementId = 10;
        List<Integer> shellIds = List.of(1, 2);

        Prelevement prelevement = new Prelevement();
        prelevement.setIdPrelevement(prelevementId);
        prelevement.setMontant(300.0);
        prelevement.setDateOperation(LocalDate.of(2025, 1, 10));

        Shell s1 = new Shell();
        s1.setIdShell(1);
        s1.setStatut(Statut.EN_ATTENTE);
        s1.setMontant(150.0);

        Shell s2 = new Shell();
        s2.setIdShell(2);
        s2.setStatut(Statut.EN_ATTENTE);
        s2.setMontant(150.0);


        when(prelevementRepository.findById(prelevementId)).thenReturn(Optional.of(prelevement));
        when(shellRepository.findAllById(shellIds)).thenReturn(List.of(s1, s2));
        when(prelevementRepository.save(any(Prelevement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrelevementDetailsResponse response =
                prelevementService.assignShellsManually(prelevementId, shellIds);

        assertNotNull(response);
        assertNotNull(response.getPrelevement());
        assertEquals(prelevementId, response.getPrelevement().getIdPrelevement());

        List<Shell> attached = response.getShells();
        assertEquals(2, attached.size());
        attached.forEach(shell -> {
            assertEquals(Statut.OK, shell.getStatut(), "Chaque shell doit être en statut OK");
            assertNotNull(shell.getPrelevement(), "Chaque shell doit être lié à un prélèvement");
            assertEquals(prelevementId, shell.getPrelevement().getIdPrelevement());
        });

        verify(shellRepository, times(1)).saveAll(anyList());
        verify(prelevementRepository, times(1)).save(any(Prelevement.class));
        verify(notificationService, times(2))
                .resolveByRef(any(), anyInt());
    }
}
