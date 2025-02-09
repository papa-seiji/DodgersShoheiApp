package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.DodgersWins;
import com.example.dodgersshoheiapp.repository.DodgersWinsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DodgersWinsService {
    private final DodgersWinsRepository dodgersWinsRepository;

    public DodgersWinsService(DodgersWinsRepository dodgersWinsRepository) {
        this.dodgersWinsRepository = dodgersWinsRepository;
    }

    public void saveDodgersWins(DodgersWins dodgersWins) {
        dodgersWinsRepository.save(dodgersWins);
    }

    public List<DodgersWins> getAllDodgersWins() {
        return dodgersWinsRepository.findAll();
    }
}
