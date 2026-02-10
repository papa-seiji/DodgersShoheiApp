package com.example.dodgersshoheiapp.controller.fragment;

import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;
import com.example.dodgersshoheiapp.repository.OhtaniPitchingGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fragments")
public class ScorebookPitchingFragmentController {

    private final OhtaniPitchingGameRepository pitchingGameRepository;

    public ScorebookPitchingFragmentController(
            OhtaniPitchingGameRepository pitchingGameRepository) {
        this.pitchingGameRepository = pitchingGameRepository;
    }

    /**
     * home用 Pitching 最新評価
     */
    @GetMapping("/pitching2026")
    public String pitching2026(Model model) {

        OhtaniPitchingGame latest = pitchingGameRepository.findTopByOrderByGameDateDesc();

        if (latest != null) {

            model.addAttribute(
                    "latestPitchingGameDate",
                    latest.getGameDate());

            String rank = latest.getFormValue(); // S A B C
            model.addAttribute("latestPitchingFormRank", rank);

            String emoji = switch (rank) {
                case "S" -> "🔥";
                case "A" -> "😊";
                case "B" -> "😳";
                case "C" -> "😣";
                default -> "🧊";
            };
            model.addAttribute("latestPitchingFormEmoji", emoji);

            model.addAttribute(
                    "latestPitchingComment",
                    latest.getComment());
        }

        return "fragments/Pitching2026_ScoreBook";
    }
}
