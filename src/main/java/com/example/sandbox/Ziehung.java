package com.example.sandbox;

import java.time.LocalDate;
import java.util.List;

public record Ziehung(LocalDate tagDerZiehung, List<Integer> zahlen, Integer zusatz, Integer superzahl) {}