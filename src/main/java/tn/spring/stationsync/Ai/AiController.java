package tn.spring.stationsync.Ai;

import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Dtos.AiNlQueryRequest;
import tn.spring.stationsync.Dtos.AiNlQueryResponse;

import java.util.Collections;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OpenRouterAiClient aiClient;
    private final AiSqlService aiSqlService;

    public AiController(OpenRouterAiClient aiClient, AiSqlService aiSqlService) {
        this.aiClient = aiClient;
        this.aiSqlService = aiSqlService;
    }

    // ✅ NL → SQL (avec exécution optionnelle)
    @PostMapping("/nl-query")
    public AiNlQueryResponse handleNlQuery(@RequestBody AiNlQueryRequest request) {
        boolean execute = request.getExecute() != null && request.getExecute();

        // 1️⃣ IA → Génère le SQL
        String sql = aiClient.generateSqlFromQuestion(
                request.getQuestion(),
                request.getContextTable()
        );

        // 2️⃣ Exécution réelle si demandé
        if (execute) {
            AiNlQueryResponse resp = aiSqlService.executeSelect(sql);

            // 3️⃣ Deuxième appel IA → explication
            String explanation = aiClient.explainQueryResult(
                    request.getQuestion(),
                    resp.getSql(),
                    resp.getRowsPreview()
            );

            resp.setAnswerText(explanation);
            return resp;
        }

        // 4️⃣ Sinon (mode démo : pas d'exécution)
        AiNlQueryResponse response = new AiNlQueryResponse();
        response.setSql(sql);
        response.setExecuted(false);
        response.setRowCount(0);
        response.setRowsPreview(Collections.emptyList());
        response.setAnswerText("Requête SQL générée, non exécutée (mode démo).");
        return response;
    }
}
