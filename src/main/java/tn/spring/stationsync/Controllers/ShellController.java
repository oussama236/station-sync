package tn.spring.stationsync.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Dtos.ShellFilterResponse;
import tn.spring.stationsync.Dtos.ShellSearchCriteria;
import tn.spring.stationsync.Entities.NatureOperation;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Station;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Services.IShellService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/Shell")


public class ShellController {


    @Autowired
    IShellService shellService;



    @GetMapping("/shells")
    @ResponseBody
    public ResponseEntity<?> getShells() {
        List<Shell> listShells = shellService.getAllShells();

        if (listShells.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun Shell disponible.");
        }

        ShellFilterResponse response = new ShellFilterResponse(listShells);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/shells/{shell-id}")
    @ResponseBody
    public Shell getShell(@PathVariable ("shell-id") Integer idShell) {

        return shellService.getShell(idShell);
    }


    @PostMapping("/addshell")
    @ResponseBody
    public  Shell saveShell(@RequestBody Shell shell){
        Shell s  = shellService.saveShell(shell);
        return s;
    }


    @PutMapping("/update-shell/{id}")
    @ResponseBody
    public Shell updateShell(@PathVariable Integer id,@RequestBody Shell s) {
        s.setIdShell(id);
        Shell shell = shellService.updateShell(s);
        return shell;
    }


    @DeleteMapping("/remove-shell/{shell-id}")
    public void removeShell(@PathVariable("shell-id") Integer idShell) {
        shellService.deleteShell(idShell);
    }


    @PutMapping("/update-statuts")
    public String updateStatuts() {
        shellService.updateStatutsWhenPrelevementDue();
        return "Shell statuts updated (VIDE → EN_ATTENTE if needed).";
    }


    @GetMapping("/shells/statut/{statut}")

    //POSTMAN                      // FrontEnd
    //statut/EN_ATTENTE            // /shells?statut=EN_ATTENTE
    //statut/VIDE                  // /shells?statut=VIDE
    //statut/OK                    // /shells?statut=OK

    public List<Shell> getShellsByStatut(@PathVariable Statut statut) {

        return shellService.getShellsByStatut(statut);
    }


    // ✅ 🔍 NEW: Filter Shells by natureOperation, station and statuts
    @GetMapping("/shells/filter")
    public ResponseEntity<?> getFilteredShells(
            @RequestParam(required = false) NatureOperation category,
            @RequestParam(required = false) Station station,
            @RequestParam(required = false) Statut statut
    ) {
        List<Statut> statutList = new ArrayList<>();

        if (statut != null) {
            statutList.add(statut);
        }

        List<Shell> result = shellService.filterShells(category, station, statutList);

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun Shell trouvé avec les critères fournis.");
        }

        ShellFilterResponse response = new ShellFilterResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/advanced-search")
    public List<Shell> searchShells(@RequestBody ShellSearchCriteria criteria) {
        return shellService.searchShells(criteria);
    }


    //TEST
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}


