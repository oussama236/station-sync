package tn.spring.stationsync.Ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenRouterAiClient {

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

    public String generateSqlFromQuestion(String userMessage, String contextTable) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();

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

            if (root == null
                    || !root.has("choices")
                    || !root.get("choices").isArray()
                    || root.get("choices").isEmpty()) {
                throw new AiUnavailableException("Le service AI est temporairement indisponible. Veuillez réessayer plus tard.");
            }

            JsonNode firstChoice = root.get("choices").get(0);
            JsonNode messageNode = firstChoice.get("message");

            if (messageNode == null || !messageNode.has("content")) {
                throw new AiUnavailableException("Réponse invalide du service AI. Veuillez réessayer plus tard.");
            }

            return messageNode.get("content").asText().trim();

        } catch (ResourceAccessException e) {
            throw new AiUnavailableException("Le service AI est temporairement indisponible ou surchargé. Veuillez réessayer plus tard.");
        } catch (RestClientResponseException e) {
            throw new AiUnavailableException("Le service AI a retourné une erreur. Veuillez réessayer plus tard.");
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Une erreur est survenue lors de la communication avec le service AI.");
        }
    }

    public String explainQueryResult(String question, String sql, List<Map<String, Object>> rows) {
        try {
            int max = Math.min(rows.size(), 10);
            List<Map<String, Object>> preview = rows.subList(0, max);

            String resultJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(preview);

            String systemPrompt = """
You are StationSync's intelligent financial assistant.

Your goal:
Given a user's French question, the SQL query that was executed, and its JSON results,
write a short, natural explanation in French of what the data shows.

Rules:
- Always answer in plain French sentences, never in tags or code.
- Do not show SQL or JSON.
- If there is one result, describe it clearly (station, montant, date, statut...).
- If there are multiple, summarize how many and give totals or patterns.
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
            if (root == null || !root.has("choices")) {
                throw new AiUnavailableException("Le service AI est temporairement indisponible. Veuillez réessayer plus tard.");
            }

            JsonNode content = root.get("choices").get(0).get("message").get("content");
            if (content == null) {
                throw new AiUnavailableException("Le service AI est temporairement indisponible. Veuillez réessayer plus tard.");
            }

            return content.asText();

        } catch (ResourceAccessException e) {
            throw new AiUnavailableException("Le service AI est temporairement indisponible ou surchargé. Veuillez réessayer plus tard.");
        } catch (RestClientResponseException e) {
            throw new AiUnavailableException("Le service AI a retourné une erreur. Veuillez réessayer plus tard.");
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Une erreur est survenue lors de la communication avec le service AI.");
        }
    }
}