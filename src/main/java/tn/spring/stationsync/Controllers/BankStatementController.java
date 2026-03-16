package tn.spring.stationsync.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.spring.stationsync.Dtos.BankStatementResponse;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Services.BankStatementPdfService;
import tn.spring.stationsync.Services.IBanqueService;
import tn.spring.stationsync.Services.IPrelevementService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankStatementController {

    private final IBanqueService banqueService;
    private final IPrelevementService prelevementService;
    private final BankStatementPdfService bankStatementPdfService;



    @GetMapping("/bank-statement")
    public BankStatementResponse getBankStatement(
            @RequestParam(required = false) Station station,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<Banque> credits = banqueService.getFilteredBanks(
                station,
                List.of(Statut.OK),
                dateFrom,
                dateTo
        );

        List<Prelevement> debits = prelevementService.getFilteredPrelevements(
                dateFrom,
                dateTo
        );

        return new BankStatementResponse(credits, debits);
    }
    @GetMapping(value = "/bank-statement/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadBankStatementPdf(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<Banque> credits = banqueService.getFilteredBanks(null, List.of(Statut.OK), dateFrom, dateTo);
        List<Prelevement> debits = prelevementService.getFilteredPrelevements(dateFrom, dateTo);

        byte[] pdf = bankStatementPdfService.generatePdf(credits, debits, dateFrom, dateTo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bank-statement.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

}
