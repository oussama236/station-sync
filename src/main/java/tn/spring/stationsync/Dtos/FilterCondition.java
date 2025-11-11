package tn.spring.stationsync.Dtos;

public class FilterCondition {

    private String field;      // ex: "station"
    private String operator;   // ex: "=", ">", "<", "BETWEEN"
    private String value;      // ex: "BOUMHAL"
    private String valueTo;    // pour BETWEEN (sinon null)

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueTo() {
        return valueTo;
    }

    public void setValueTo(String valueTo) {
        this.valueTo = valueTo;
    }
}