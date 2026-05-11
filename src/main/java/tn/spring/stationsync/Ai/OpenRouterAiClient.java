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
- NO explanations, NO comments, NO markdown.
- Return ONLY the SQL query.

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

Security rules:
- ONLY SELECT queries are allowed.
- Absolutely NO:
  INSERT,
  UPDATE,
  DELETE,
  DROP,
  ALTER,
  CREATE,
  TRUNCATE,
  REPLACE.
- Never generate multiple queries.
- Never use SQL comments.
- Never explain the query.

Column rules:
- Use ONLY the exact column names defined above.
- Use snake_case and lowercase exactly as defined.

Context rules:
- If contextTable is provided:
  - shell -> focus on SHELL
  - banque -> focus on BANQUE
  - prelevement -> focus on PRELEVEMENT

Vocabulary rules:
- In StationSync, the word "facture" refers to records from the SHELL table in general.
- If the user says "facture" without specifying a type:
  include all nature_operation values:
    AVOIR,
    FACTURE_CARBURANT,
    FACTURE_LUBRIFIANT,
    LOYER.
- Do NOT filter only carburant or lubrifiant unless explicitly requested.

Nature operation mapping:
- "facture carburant"
    -> nature_operation = 'FACTURE_CARBURANT'
- "facture lubrifiant"
    -> nature_operation = 'FACTURE_LUBRIFIANT'
- "avoir"
    -> nature_operation = 'AVOIR'
- "loyer"
    -> nature_operation = 'LOYER'

Default behavior rules:
- If the user does NOT mention a statut:
  include ALL statuts by default.
- If the user does NOT mention a station:
  include ALL stations.
- If the user does NOT mention a date:
  include ALL dates.
- Never assume:
    statut = 'OK'
    statut != 'EN_ATTENTE'
    station = 'BOUMHAL'
    station = 'ZAHRA'
  unless explicitly requested.

STRICT FILTER RULE:
- If the user does NOT explicitly mention a statut,
  the generated SQL MUST NOT contain:
    statut =
    statut !=
    statut IN
    statut NOT IN

Aggregation rules:
- SUM, COUNT, AVG, percentages, totals, and statistics
  must be calculated ONLY using filters explicitly requested by the user.
- Words like:
    "somme",
    "total",
    "montant total",
    "factures"
  do NOT imply validated data only.

Display rules:
- Do NOT select technical IDs by default.
- Never include:
    id_shell,
    prelevement_id,
    id_banque,
    id_prelevement
  unless explicitly requested.
- Prefer business columns:
    date_operation,
    date_prelevement,
    montant,
    nature_operation,
    numero_facture,
    station,
    statut,
    numero_bordereau,
    numero_compte

Alias rules:
- Always use clear aliases with AS for:
    SUM,
    COUNT,
    AVG,
    ROUND,
    percentages,
    expressions,
    CASE statements.
- Never leave calculated columns without aliases.

Formatting rules:
- Use ROUND(value, 3) for monetary totals.
- Use ROUND(value, 2) for percentages.
- Avoid SELECT * unless explicitly necessary.

Date rules:
- If the user asks for a month like:
    "octobre 2025"
  use:
    BETWEEN '2025-10-01' AND '2025-10-31'
    
Business calculation rule:
- In StationSync, AVOIR represents a negative adjustment.
- When calculating the global total of factures, AVOIR amounts must be subtracted from the total.
- Formula:
    total_factures = SUM(all positive factures) - SUM(AVOIR)
- Use:
    CASE
      WHEN nature_operation = 'AVOIR' THEN -montant
      ELSE montant
    END
  for global facture totals.

Examples:
- "total des factures"
- "montant total des factures"
- "somme des factures"

must subtract AVOIR from the total automatically.

However:
- If the user explicitly asks only for:
    "factures carburant"
    "factures lubrifiant"
    "loyer"
    "avoir"
  then calculate only that category without applying the global formula.

Output rules:
- Return ONLY the SQL query.
- No markdown.
- No backticks.
- No JSON.
- No explanations.
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