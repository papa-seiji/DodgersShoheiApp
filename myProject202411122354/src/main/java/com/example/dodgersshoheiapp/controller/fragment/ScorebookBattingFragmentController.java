package com.example.dodgersshoheiapp.controller.fragment;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fragments")
public class ScorebookBattingFragmentController {

    private final OhtaniGameRepository gameRepository;

    public ScorebookBattingFragmentController(OhtaniGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @GetMapping("/batting2026")
    public String batting2026(Model model) {

        OhtaniGame latestGame = gameRepository.findLatestGame();

        if (latestGame != null) {
            model.addAttribute("latestGameDate", latestGame.getGameDate());
            model.addAttribute("latestFormRank",
                    convertFormRank(latestGame.getFormValue()));
            model.addAttribute("latestFormEmoji",
                    convertFormEmoji(latestGame.getFormValue()));
            model.addAttribute("latestComment",
                    latestGame.getComment());
        }

        return "fragments/Batting2026_ScoreBook";
    }

    /* ===== util（hogehoge_01 からそのまま） ===== */

    private String convertFormRank(Integer value) {
        if (value == null)
            return "-";
        return switch (value) {
            case 2 -> "S";
            case 1 -> "A";
            case 0 -> "B";
            case -1 -> "C";
            case -2 -> "D";
            default -> "-";
        };
    }

    private String convertFormEmoji(Integer value) {
        if (value == null)
            return "";
        return switch (value) {
            case 2 -> "🔥";
            case 1 -> "😊";
            case 0 -> "😳";
            case -1 -> "😣";
            case -2 -> "🧊";
            default -> "";
        };
    }
}
