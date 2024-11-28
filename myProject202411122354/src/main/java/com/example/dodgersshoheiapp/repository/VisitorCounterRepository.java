package com.example.dodgersshoheiapp.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.dodgersshoheiapp.model.VisitorCounter;

@Repository
public interface VisitorCounterRepository extends CrudRepository<VisitorCounter, Integer> {
}