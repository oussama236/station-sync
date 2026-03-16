package tn.spring.stationsync.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BankStatementLine {
    private LocalDate date;
    private String description;
    private BigDecimal credit;
    private BigDecimal debit;
}