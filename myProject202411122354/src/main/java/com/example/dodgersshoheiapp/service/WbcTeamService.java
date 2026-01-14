package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.WbcTeam;
import com.example.dodgersshoheiapp.repository.WbcTeamRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WbcTeamService {

    private final WbcTeamRepository teamRepository;

    public WbcTeamService(WbcTeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    /**
     * POOL別にチーム名だけを返す
     * Map<POOL, List<TEAM_NAME>>
     */
    public Map<String, List<String>> getPoolTeamsByYear(int year) {

        List<WbcTeam> teams = teamRepository.findByYearOrderByPoolAscTeamNameAsc(year);

        return teams.stream()
                .collect(Collectors.groupingBy(
                        WbcTeam::getPool,
                        LinkedHashMap::new,
                        Collectors.mapping(WbcTeam::getTeamName,
                                Collectors.toList())));
    }
}
