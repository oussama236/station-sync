package tn.spring.stationsync.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Prelevement;

import java.util.List;



    @Data
    @AllArgsConstructor
    public class BankStatementResponse {
        private List<Banque> credits;     // Banque ops (statut = OK)
        private List<Prelevement> debits; // Prélèvements
    }

