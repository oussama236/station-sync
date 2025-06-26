package tn.spring.stationsync.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Dtos.PrelevementMatchPreviewRequest;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Entities.Shell;
import tn.spring.stationsync.Services.IPrelevementService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/Prelevement")
public class PrelevementController {

    @Autowired
    IPrelevementService prelevementService;


    @PostMapping("/addPrelevement")
    @ResponseBody
    public Prelevement savePrelevement(@RequestBody Prelevement prelevement){
        return prelevementService.savePrelevement(prelevement);
    }


    @GetMapping("/prelevements")
    @ResponseBody
    public List<Prelevement> getPrelevements() {
        return prelevementService.getAllPrelevements();
    }


    @GetMapping("/prelevements/{Prelevement-id}")
    @ResponseBody
    public Prelevement getPrelevement(@PathVariable("Prelevement-id") Integer idPrelevement) {
        return prelevementService.getPrelevement(idPrelevement);
    }


    @PutMapping("/update-prelevement/{id}")
    @ResponseBody
    public Prelevement updateprelevement(@PathVariable Integer id,@RequestBody Prelevement p) {
        p.setIdPrelevement(id);
        return prelevementService.updatePrelevement(p);
    }


    @DeleteMapping("/remove-prelevement/{prelevement-id}")
    public void removeprelevement(@PathVariable("prelevement-id") Integer idPrelevement) {
        prelevementService.deletePrelevement(idPrelevement);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PrelevementDetailsResponse> getPrelevementwithResume(@PathVariable Integer id) {
        return ResponseEntity.ok(prelevementService.getPrelevementAvecResume(id));
    }


    @PostMapping("/manual-assign")
    public ResponseEntity<PrelevementDetailsResponse> affectShellsToPrelevement(@RequestBody Map<String, Object> payload) {
        Integer prelevementId = (Integer) payload.get("prelevementId");

        @SuppressWarnings("unchecked")
        List<Integer> shellIds = ((List<?>) payload.get("shellIds"))
                .stream()
                .map(id -> Integer.parseInt(id.toString()))
                .toList();

        PrelevementDetailsResponse result = prelevementService.assignShellsManually(prelevementId, shellIds);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/simulate-auto-assign")
    public ResponseEntity<PrelevementDetailsResponse> simulateAutoAssign(@RequestBody PrelevementMatchPreviewRequest request) {
        PrelevementDetailsResponse result = prelevementService.simulateAutoAssignement(request.getMontant(), request.getDateOperation());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/manual-shells")
    public ResponseEntity<List<Shell>> getShellsForManualAffectation(
            @RequestParam("dateOperation") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOperation) {

        List<Shell> shells = prelevementService.getShellsForManualAffectation(dateOperation);
        return ResponseEntity.ok(shells);
    }

}
