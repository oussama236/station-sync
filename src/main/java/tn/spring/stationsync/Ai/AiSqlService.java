package tn.spring.stationsync.Ai;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tn.spring.stationsync.Dtos.AiNlQueryResponse;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiSqlService {

    private final JdbcTemplate jdbcTemplate;

    public AiSqlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ✅ Sécuriser le SQL (seulement SELECT + LIMIT)
    public String sanitizeSql(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL invalide (null).");
        }

        String trimmed = sql.trim();

        // enlever le ; final si présent
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        String upper = trimmed.toUpperCase(Locale.ROOT);

        // doit commencer par SELECT
        if (!upper.startsWith("SELECT")) {
            throw new IllegalArgumentException("Seules les requêtes SELECT sont autorisées.");
        }

        // mots interdits
        String[] forbidden = {" UPDATE ", " DELETE ", " INSERT ", " DROP ", " ALTER ", " TRUNCATE "};
        for (String kw : forbidden) {
            if (upper.contains(kw)) {
                throw new IllegalArgumentException("Mot clé SQL interdit détecté: " + kw.trim());
            }
        }

        // si pas de LIMIT, on en rajoute un
        if (!upper.contains(" LIMIT ")) {
            trimmed = trimmed + " LIMIT 200";
        }

        return trimmed;
    }

    // ✅ Exécuter la requête SELECT et construire la réponse
    public AiNlQueryResponse executeSelect(String rawSql) {
        String safeSql = sanitizeSql(rawSql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(safeSql);

        int maxPreview = 50;
        List<Map<String, Object>> preview =
                rows.size() > maxPreview ? rows.subList(0, maxPreview) : rows;

        AiNlQueryResponse resp = new AiNlQueryResponse();
        resp.setSql(safeSql + ";"); // on remet un ; pour affichage
        resp.setExecuted(true);
        resp.setRowCount(rows.size());
        resp.setRowsPreview(preview);
        resp.setAnswerText("Résultat brut de la requête SQL (explication IA à venir).");

        return resp;
    }
}
