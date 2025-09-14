package com.example.dodgersshoheiapp.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class GameInfo {
    private final String venue;
    private final ZonedDateTime gameDateTimeJST; // JSTの開始時刻
    private final LocalDate officialDate; // MLB公式日付（米国側）

    public GameInfo(String venue, ZonedDateTime gameDateTimeJST, LocalDate officialDate) {
        this.venue = venue;
        this.gameDateTimeJST = gameDateTimeJST;
        this.officialDate = officialDate;
    }

    public String getVenue() {
        return venue;
    }

    public ZonedDateTime getGameDateTimeJST() {
        return gameDateTimeJST;
    }

    public LocalDate getOfficialDate() {
        return officialDate;
    }
}
