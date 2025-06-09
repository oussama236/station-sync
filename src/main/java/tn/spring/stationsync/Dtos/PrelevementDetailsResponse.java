package tn.spring.stationsync.Dtos;

import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;

import java.util.List;

    public class PrelevementDetailsResponse {
        private Prelevement prelevement;
        private List<Shell> shells;
        private double montantTotalAffecté;
        private double montantRestant;
        private int nombreShells;

        public PrelevementDetailsResponse(Prelevement prelevement, List<Shell> shells) {
            this.prelevement = prelevement;
            this.montantTotalAffecté = shells.stream().mapToDouble(Shell::getMontant).sum();
            this.montantRestant = prelevement.getMontant() - this.montantTotalAffecté;
            this.nombreShells = shells.size();
        }

        public Prelevement getPrelevement() {
            return prelevement;
        }

        public List<Shell> getShells() {
            return shells;
        }

        public double getMontantTotalAffecté() {
            return montantTotalAffecté;
        }

        public double getMontantRestant() {
            return montantRestant;
        }

        public int getNombreShells() {
            return nombreShells;
        }
    }


