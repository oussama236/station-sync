package tn.spring.stationsync.Ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenRouterAiClient {

    // 🔹 Prompt spécialisé pour NL → SQL
    private static final String SQL_SYSTEM_PROMPT = """
You are StationSync's SQL generator.

Goal:
- From a French question about StationSync data,
- Generate ONLY one safe SQL SELECT query for MySQL.
- NO explanations, NO comments, NO markdown. Only the SQL.

Database:
- Table SHELL:
  - id_shell (PRIMARY KEY, AUTO_INCREMENT)
  - date_operation (DATE)
  - date_prelevement (DATE)
  - montant (DOUBLE)
  - nature_operation (ENUM: AVOIR, FACTURE_CARBURANT, FACTURE_LUBRIFIANT, LOYER)
  - numero_facture (VARCHAR)
  - station (ENUM: BOUMHAL, ZAHRA)
  - statut (ENUM: EN_ATTENTE, OK, VIDE)
  - prelevement_id (INT, FOREIGN KEY)

- Table BANQUE:
  - id_banque
  - date_operation
  - numero_bordereau
  - numero_compte
  - nature_operation_bank (ENUM: ESPECE_PISTE, ESPECE_SHOP, CARTE_BANK, TRAITE)
  - station (ENUM: BOUMHAL, ZAHRA)
  - montant
  - statut (ENUM: EN_ATTENTE, OK)

- Table PRELEVEMENT:
  - id_prelevement
  - date_operation
  - numero_compte
  - montant
  - statut (ENUM: EN_ATTENTE, OK)

Rules:
- Only SELECT queries. Absolutely NO INSERT, UPDATE, DELETE, DROP, ALTER, CREATE.
- Use the exact column names as defined above (snake_case, lowercase).
- Prefer filtering by date_operation, station, montant, nature_operation / nature_operation_bank, statut.
- If the user asks for "octobre 2025", use BETWEEN '2025-10-01' AND '2025-10-31'.
- Always add a LIMIT 200 at the end (unless the user explicitly asks a small number).
- If contextTable is provided (shell / banque / prelevement), focus on that table.

Output:
- Return ONLY the SQL query as plain text.
- Do NOT surround with backticks.
- Do NOT return JSON.
""";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public OpenRouterAiClient(RestTemplate restTemplate,
                              ObjectMapper objectMapper,
                              @Value("${ai.api.url}") String apiUrl,
                              @Value("${ai.api.key}") String apiKey,
                              @Value("${ai.api.model}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * NL → SQL : génère une requête SELECT à partir d'une question en français.
     */
    public String generateSqlFromQuestion(String userMessage, String contextTable) {

        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt spécialisé SQL
        messages.add(Map.of(
                "role", "system",
                "content", SQL_SYSTEM_PROMPT
        ));

        String userContent = "Question en français: " + userMessage +
                "\nTable de contexte (optionnelle): " + (contextTable == null ? "none" : contextTable);

        messages.add(Map.of(
                "role", "user",
                "content", userContent
        ));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.add("HTTP-Referer", "https://stationsync.local");
        headers.add("X-Title", "StationSync NL-SQL");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                JsonNode.class
        );

        JsonNode root = response.getBody();
        System.out.println("=== OpenRouter SQL raw response ===");
        System.out.println(root);

        if (root == null
                || !root.has("choices")
                || !root.get("choices").isArray()
                || root.get("choices").isEmpty()) {
            return "SELECT 1;";
        }

        JsonNode firstChoice = root.get("choices").get(0);
        JsonNode messageNode = firstChoice.get("message");

        if (messageNode == null || !messageNode.has("content")) {
            return "SELECT 1;";
        }

        // Ici, content = la requête SQL en texte brut
        String sql = messageNode.get("content").asText();
        return sql.trim();
    }

    /**
     * Explique le résultat d'une requête SQL en français naturel.
     */
    public String explainQueryResult(String question, String sql, List<Map<String, Object>> rows) {
        try {
            // On limite l'aperçu (évite d'envoyer 100 lignes)
            int max = Math.min(rows.size(), 10);
            List<Map<String, Object>> preview = rows.subList(0, max);

            // Crée le contenu JSON lisible par l'IA
            String resultJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(preview);

            String systemPrompt = """
You are StationSync's intelligent assistant.

Your goal:
Given a user's French question, the SQL query that was executed, and its JSON results,
write a short, natural explanation in French of what the data shows.

Rules:
- Always answer in plain French sentences, never in tags or code.
- Do not show SQL or JSON.
- If there is one result, describe it clearly (station, montant, date, statut...).
- If there are multiple, summarize how many and give totals or patterns.
- Example of style:
  "Il y a 3 factures carburant à Boumhal en octobre 2025 pour un total de 12 000 TND."
""";

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            String userContent = "Question: " + question +
                    "\nSQL:\n" + sql +
                    "\nResults JSON:\n" + resultJson;

            messages.add(Map.of("role", "user", "content", userContent));

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.add("HTTP-Referer", "https://stationsync.local");
            headers.add("X-Title", "StationSync Result Explainer");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            if (root == null || !root.has("choices")) return "Aucune explication IA reçue.";

            JsonNode content = root.get("choices").get(0).get("message").get("content");
            return content == null ? "Aucune explication IA reçue." : content.asText();

        } catch (Exception e) {
            System.out.println("Erreur IA explication : " + e.getMessage());
            return "Erreur lors de la génération de l'explication IA.";
        }
    }
}
