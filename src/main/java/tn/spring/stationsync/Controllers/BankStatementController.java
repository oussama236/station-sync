package tn.spring.stationsync.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.spring.stationsync.Dtos.BankStatementResponse;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Services.IBanqueService;
import tn.spring.stationsync.Services.IPrelevementService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankStatementController {

    private final IBanqueService banqueService;
    private final IPrelevementService prelevementService;



    @GetMapping("/bank-statement")
    public BankStatementResponse getBankStatement(
            @RequestParam(required = false) Station station
    ) {
        List<Banque> credits = banqueService.getFilteredBanks(
                station,
                List.of(Statut.OK) // only OK on the credit side
        );

        List<Prelevement> debits = prelevementService.getAllPrelevements();

        return new BankStatementResponse(credits, debits);
    }
}
