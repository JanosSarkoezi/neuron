package com.example.sandbox.markov;

import com.example.sandbox.lotto.DataReader;
import com.example.sandbox.lotto.Lotto6aus49;
import com.example.sandbox.neuron.Matrix;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class LottoMarkov {

    public static void main(String[] args) {
        try {
            // 1. Dateipfad abrufen (unverändert)
            ClassLoader classLoader = LottoMarkov.class.getClassLoader();
            URI uri = Objects.requireNonNull(classLoader.getResource("lotto_6aus49_ab_02.12.2000.txt")).toURI();
            Path filePath = Path.of(uri);

            // 2. Alle Daten einlesen
            System.out.println("Lese alle Daten aus der Datei: " + filePath.toAbsolutePath());
            List<Lotto6aus49> alleZiehungen = DataReader.readData(filePath);
            System.out.println("Anzahl aller gelesenen Ziehungen: " + alleZiehungen.size());

            // 3. Filterkriterium definieren (z.B. alle Ziehungen ab dem 01.01.2020)
            LocalDate filterDatum = LocalDate.of(2020, 1, 1);

            // 4. Daten nach dem Datum filtern
            List<Lotto6aus49> gefilterteZiehungen = alleZiehungen.stream()
                    .filter(ziehung -> ziehung.datum().isAfter(filterDatum) || ziehung.datum().isEqual(filterDatum))
                    .toList();

            System.out.println("Anzahl der Ziehungen nach dem " + filterDatum + ": " + gefilterteZiehungen.size());

            // 5. Markov-Matrix initialisieren
            int maxLottoNumber = 49;
            MarkovMatrix markov = new MarkovMatrix(maxLottoNumber);

            // 6. Nur die gefilterten Ziehungen zur Matrix hinzufügen
            System.out.println("Erstelle die Übergangsmatrix mit den gefilterten Daten...");
            for (Lotto6aus49 ziehung : gefilterteZiehungen) {
                markov.addZiehung(ziehung.zahlen());
            }

            // 7. Matrix normalisieren und ausgeben
            Matrix probabilityMatrix = markov.buildProbabilityMatrix();
            System.out.println("Markov-Matrix erfolgreich erstellt.");
            System.out.println("Die Matrix hat die Dimension " + probabilityMatrix.rows() + "x" + probabilityMatrix.cols() + ".");

            // Beispielhafte Ausgabe eines Ausschnitts...
            // (Code für die Ausgabe bleibt gleich)

        } catch (Exception e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }
    }
}