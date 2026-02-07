package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OhtaniPitchingGameController {

    private final OhtaniPitchingGameRepository pitchingGameRepository;

    @GetMapping("/hogehoge_04")
    public String showPitchingMonth(
            @RequestParam(name = "month", required = false) Integer month,
            Model model) {

        int year = 2026;
        if (month == null)
            month = 4;

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<OhtaniPitchingGame> monthGames = pitchingGameRepository.findByMonthRange(startDate, endDate);

        model.addAttribute("month", month);
        model.addAttribute("monthGames", monthGames);

        return "hogehoge_04";
    }
}
