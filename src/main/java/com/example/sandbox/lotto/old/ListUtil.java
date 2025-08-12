package com.example.sandbox.lotto.old;

import java.util.List;
import java.util.Optional;

public class ListUtil {

    /**
     * Entfernt die letzten z Elemente aus der Liste und gibt das z-te Element von hinten zurück.
     * Wenn z = 0 ist, wird nichts gelöscht und Optional.empty() zurückgegeben.
     *
     * @param liste Die Liste, die in-place verändert wird.
     * @param z Anzahl der zu löschenden Elemente (>= 0).
     * @return Optional mit dem z-ten Element von hinten, oder Optional.empty() wenn z<=0.
     * @throws IllegalArgumentException wenn z > liste.size().
     */
    public static <T> Optional<T> removeLastAndSave(List<T> liste, int z) {
        if (z > liste.size()) {
            throw new IllegalArgumentException("Liste ist zu klein für " + z + " Elemente zum Entfernen.");
        }
        if (z <= 0) {
            return Optional.empty();
        }

        T gespeichert = liste.get(liste.size() - z);
        liste.subList(liste.size() - z, liste.size()).clear();
        return Optional.of(gespeichert);
    }
}