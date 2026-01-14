// package com.example.dodgersshoheiapp.repository;

// public class WbcPoolMatchRepository {

// }

package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WbcPoolMatchRepository extends JpaRepository<WbcPoolMatch, Long> {

    List<WbcPoolMatch> findByYear(Integer year);

    List<WbcPoolMatch> findByYearAndPool(Integer year, String pool);
}
