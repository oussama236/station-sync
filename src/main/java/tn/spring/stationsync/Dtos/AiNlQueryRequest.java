package tn.spring.stationsync.Dtos;

public class AiNlQueryRequest {

    private String question;      // la question en français
    private String contextTable;  // ex: "shell", "banque", "prelevement"
    private Boolean execute;      // optionnel (pour plus tard)

    public AiNlQueryRequest() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getContextTable() {
        return contextTable;
    }

    public void setContextTable(String contextTable) {
        this.contextTable = contextTable;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }
}
