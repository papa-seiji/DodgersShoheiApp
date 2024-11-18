package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.Counter;
import com.example.dodgersshoheiapp.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    @Autowired
    private CounterRepository counterRepository;

    public Counter getCounter() {
        return counterRepository.findByCounterName("MainCounter");
    }

    public Counter updateCounterValue(int increment) {
        Counter counter = counterRepository.findByCounterName("MainCounter");
        counter.setValue(counter.getValue() + increment);
        counterRepository.save(counter);
        return counter;
    }
}
