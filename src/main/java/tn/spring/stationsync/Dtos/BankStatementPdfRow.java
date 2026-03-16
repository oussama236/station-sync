package tn.spring.stationsync.Dtos;

public class BankStatementPdfRow {

    private String creditDate;
    private String creditNature;
    private String creditAmount;

    private String debitDate;
    private String debitNature;
    private String debitAmount;

    public BankStatementPdfRow() {
    }

    public BankStatementPdfRow(String creditDate, String creditNature, String creditAmount,
                               String debitDate, String debitNature, String debitAmount) {
        this.creditDate = creditDate;
        this.creditNature = creditNature;
        this.creditAmount = creditAmount;
        this.debitDate = debitDate;
        this.debitNature = debitNature;
        this.debitAmount = debitAmount;
    }

    public String getCreditDate() {
        return creditDate;
    }

    public void setCreditDate(String creditDate) {
        this.creditDate = creditDate;
    }

    public String getCreditNature() {
        return creditNature;
    }

    public void setCreditNature(String creditNature) {
        this.creditNature = creditNature;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getDebitDate() {
        return debitDate;
    }

    public void setDebitDate(String debitDate) {
        this.debitDate = debitDate;
    }

    public String getDebitNature() {
        return debitNature;
    }

    public void setDebitNature(String debitNature) {
        this.debitNature = debitNature;
    }

    public String getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(String debitAmount) {
        this.debitAmount = debitAmount;
    }


}
