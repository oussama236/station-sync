package tn.spring.stationsync.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.spring.stationsync.Entities.Prelevement;
import tn.spring.stationsync.Repositories.PrelevementRepository;

import java.util.List;

@Service
public class PrelevementServiceImpl implements IPrelevementService{

    @Autowired
    private PrelevementRepository prelevementRepository;


    @Override
    public Prelevement savePrelevement(Prelevement prelevement) {
        return prelevementRepository.save(prelevement);
    }

    @Override
    public List<Prelevement> getAllPrelevements() {
        return prelevementRepository.findAll();
    }

    @Override
    public Prelevement getPrelevement(Integer idPrelevement) {
        return prelevementRepository.findById(idPrelevement).get();
    }

    @Override
    public void deletePrelevement(Integer idPrelevement) {

        prelevementRepository.deleteById(idPrelevement);
    }

    @Override
    public Prelevement updatePrelevement(Prelevement p) {
        return prelevementRepository.save(p);
    }
}
