package com.example.sandbox.lotto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record Lotto6aus49(
        LocalDate datum,
        List<Integer> zahlen,
        Optional<Integer> zusatzzahl,
        int superzahl
) {}