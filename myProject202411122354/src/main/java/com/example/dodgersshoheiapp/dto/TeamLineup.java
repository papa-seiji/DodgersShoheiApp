package com.example.dodgersshoheiapp.dto;

import java.util.List;

public record TeamLineup(String teamName, Pitcher probablePitcher, java.util.List<PlayerEntry> lineup) {
}
