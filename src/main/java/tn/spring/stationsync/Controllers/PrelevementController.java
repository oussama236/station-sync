package tn.spring.stationsync.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Services.IPrelevementService;

import java.util.List;


@RestController
@RequestMapping("/Prelevement")
public class PrelevementController {

    @Autowired
    IPrelevementService prelevementService;


    @PostMapping("/addPrelevement")
    @ResponseBody
    public Prelevement savePrelevement(@RequestBody Prelevement prelevement){
        Prelevement p = prelevementService.savePrelevement(prelevement);
        return p;
    }


    @GetMapping("/prelevements")
    @ResponseBody
    public List<Prelevement> getPrelevements() {
        List<Prelevement> listPrelevements = prelevementService.getAllPrelevements();
        return listPrelevements;
    }


    @GetMapping("/prelevements/{Prelevement-id}")
    @ResponseBody
    public Prelevement getPrelevement(@PathVariable ("Prelevement-id") Integer idPrelevement) {
        return prelevementService.getPrelevement(idPrelevement);
    }


    @PutMapping("/update-prelevement/{id}")
    @ResponseBody
    public Prelevement updateprelevement(@PathVariable Integer id,@RequestBody Prelevement p) {
        p.setIdPrelevement(id);
        Prelevement prelevement = prelevementService.updatePrelevement(p);
        return prelevement;
    }


    @DeleteMapping("/remove-prelevement/{prelevement-id}")
    public void removeprelevement(@PathVariable("prelevement-id") Integer idPrelevement) {
        prelevementService.deletePrelevement(idPrelevement);
    }
    @GetMapping("/{id}")
    public ResponseEntity<PrelevementDetailsResponse> getPrelevementwithResume(@PathVariable Integer id) {
        return ResponseEntity.ok(prelevementService.getPrelevementAvecResume(id));
    }

    @PostMapping("/assign-shells/{id}")
    public ResponseEntity<Prelevement> assignShells(@PathVariable Integer id) {
        Prelevement p = prelevementService.getPrelevement(id);
        Prelevement updated = prelevementService.assignShellsToPrelevement(p);
        return ResponseEntity.ok(updated);
    }

}
