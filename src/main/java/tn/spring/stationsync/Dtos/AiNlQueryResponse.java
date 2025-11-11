package tn.spring.stationsync.Dtos;

import java.util.List;
import java.util.Map;

public class AiNlQueryResponse {

    private String answerText;   // explication finale IA (plus tard)
    private String sql;          // SQL généré
    private boolean executed;    // est-ce qu'on l’a exécuté ?
    private int rowCount;        // nb de lignes (si exécuté)
    private List<Map<String, Object>> rowsPreview; // preview des lignes

    public AiNlQueryResponse() {
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public List<Map<String, Object>> getRowsPreview() {
        return rowsPreview;
    }

    public void setRowsPreview(List<Map<String, Object>> rowsPreview) {
        this.rowsPreview = rowsPreview;
    }
}
