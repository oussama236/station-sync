package tn.spring.stationsync.Dtos;


public class AiChatRequest {

    private String message;
    private String contextTable; // e.g. "shell", "prelevement", "banque"

    public AiChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContextTable() {
        return contextTable;
    }

    public void setContextTable(String contextTable) {
        this.contextTable = contextTable;
    }
}
