package tn.spring.stationsync.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Dtos.ShellFilterResponse;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Entities.Statut;
import tn.spring.stationsync.Services.IShellService;
import java.util.List;

@RestController
@RequestMapping("/Shell")


public class ShellController {


    @Autowired
    IShellService shellService;



    @GetMapping("/shells")
    @ResponseBody
    public List<Shell> getShells() {
        List<Shell> listShells = shellService.getAllShells();
        return listShells;
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


    //filter?category=FACTURE_CARBURANT&site=ZAHRA&statuts=OK
    @GetMapping("/shells/filter")
    public ResponseEntity<?> getFilteredShells(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String site,
            @RequestParam(required = false) List<Statut> statuts
    ) {
        List<Shell> result = shellService.findFiltered(category, site, statuts);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun Shell trouvé avec les critères fournis.");
        }
        ShellFilterResponse response = new ShellFilterResponse(result);
        return ResponseEntity.ok(response);
    }



    //TEST
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}


