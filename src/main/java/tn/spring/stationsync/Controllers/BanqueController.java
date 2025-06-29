package tn.spring.stationsync.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Entities.Banque;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Services.IBanqueService;

import java.util.List;

@RestController
@RequestMapping("/Banque")

public class BanqueController {

    @Autowired
    IBanqueService banqueService;


    @PostMapping("/addOperation")
    @ResponseBody
    public  Banque saveBank(@RequestBody Banque bank){
        Banque banque = banqueService.saveBank(bank);
        return banque;
    }


    @GetMapping("/operations")
    @ResponseBody
    public List<Banque> getBanks() {
        List<Banque> listBanks = banqueService.getAllBanks();
        return listBanks;
    }


    @GetMapping("/operations/{Banque-id}")
    @ResponseBody
    public Banque getBank(@PathVariable ("Banque-id") Integer idBanque) {

        return banqueService.getBanque(idBanque);
    }


    @PutMapping("/update-operation/{id}")
    @ResponseBody
    public Banque updateBanque(@PathVariable Integer id,@RequestBody Banque b) {
        b.setIdBanque(id);
        Banque banque = banqueService.updateBanque(b);
        return banque;
    }


    @DeleteMapping("/remove-operation/{banque-id}")
    public void removeBanque(@PathVariable("banque-id") Integer idBanque) {
        banqueService.deleteBank(idBanque);
    }


    @PutMapping("/statut/{id}")
    public void updateBankStatut(@PathVariable Integer id) {
        banqueService.updateBankStatut(id);
    }


    @GetMapping("/banques/filter")
    public List<Banque> filterBanques(
            @RequestParam(required = false) Station station,
            @RequestParam(required = false) List<Statut> statuts) {
        return banqueService.getFilteredBanks(station, statuts);
    }
}
