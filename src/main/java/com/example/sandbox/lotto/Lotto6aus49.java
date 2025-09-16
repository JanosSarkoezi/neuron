package com.example.sandbox.lotto;

import java.time.LocalDate;
import java.util.List;

public record Lotto6aus49(
        LocalDate datum,
        List<Integer> zahlen,
        int zusatzzahl,
        int superzahl
) {}