package tn.spring.stationsync.Services;


import tn.spring.stationsync.Dtos.PrelevementDetailsResponse;
import tn.spring.stationsync.Entities.Prelevement;

import java.util.List;

public interface IPrelevementService {

    Prelevement savePrelevement(Prelevement prelevement);

    // Retrieve all Prelevements
    List<Prelevement> getAllPrelevements();

    // Find a Prelevement by ID
    Prelevement getPrelevement (Integer idPrelevement);


    // Delete a Prelevement by ID
    void deletePrelevement(Integer idPrelevement);

    //Update Prelevement by ID
    Prelevement updatePrelevement (Prelevement  p);

    PrelevementDetailsResponse getPrelevementAvecResume(Integer id);

    Prelevement assignShellsToPrelevement(Prelevement prelevement);


}

