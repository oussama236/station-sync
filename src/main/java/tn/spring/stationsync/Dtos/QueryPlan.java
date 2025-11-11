package tn.spring.stationsync.Dtos;

import java.util.List;

public class QueryPlan {

    private String targetTable;           // "shell", "banque", "prelevement"
    private String operation;             // ex: "SEARCH"
    private List<FilterCondition> filters;
    private Integer limit;                // ex: 100

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public List<FilterCondition> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterCondition> filters) {
        this.filters = filters;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}