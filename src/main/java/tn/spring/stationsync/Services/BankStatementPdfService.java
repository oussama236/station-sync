package tn.spring.stationsync.Services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tn.spring.stationsync.Dtos.BankStatementPdfRow;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Prelevement;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankStatementPdfService {

    private final TemplateEngine templateEngine;

    public byte[] generatePdf(List<Banque> credits, List<Prelevement> debits,
                              LocalDate dateFrom, LocalDate dateTo) {
        try {
            List<BankStatementPdfRow> rows = buildRows(credits, debits);

            double totalCredits = credits.stream()
                    .mapToDouble(Banque::getMontant)
                    .sum();

            double totalDebits = debits.stream()
                    .mapToDouble(Prelevement::getMontant)
                    .sum();

            double balance = totalCredits - totalDebits;

            Context context = new Context();
            context.setVariable("dateFrom", dateFrom);
            context.setVariable("dateTo", dateTo);
            context.setVariable("rows", rows);
            context.setVariable("totalCredits", String.format("+ %.3f", totalCredits));
            context.setVariable("totalDebits", String.format("- %.3f", totalDebits));
            context.setVariable("balance", String.format("%.2f", balance));

            String html = templateEngine.process("bank-statement", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            String baseUrl = getClass().getResource("/static/").toExternalForm();
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private List<BankStatementPdfRow> buildRows(List<Banque> credits, List<Prelevement> debits) {
        List<BankStatementPdfRow> rows = new ArrayList<>();

        int max = Math.max(credits.size(), debits.size());

        for (int i = 0; i < max; i++) {
            Banque credit = i < credits.size() ? credits.get(i) : null;
            Prelevement debit = i < debits.size() ? debits.get(i) : null;

            rows.add(new BankStatementPdfRow(
                    credit != null ? credit.getDateOperation().toString() : "",
                    credit != null ? String.valueOf(credit.getNatureOperationBank()) : "",
                    credit != null ? "+ " + String.format("%.3f", credit.getMontant()) : "",

                    debit != null ? debit.getDateOperation().toString() : "",
                    debit != null ? "Prélèvement" : "",
                    debit != null ? "- " + String.format("%.3f", debit.getMontant()) : ""
            ));
        }

        return rows;
    }
}