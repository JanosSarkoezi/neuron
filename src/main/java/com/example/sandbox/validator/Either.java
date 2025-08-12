package com.example.sandbox.validator;

import java.util.Optional;
import java.util.function.Function;

/**
 * <p>Repräsentiert einen Wert, der entweder vom Typ {@code L} (Left) oder vom Typ {@code R} (Right) ist.</p>
 *
 * <p>Das Either-Muster wird oft in der funktionalen Programmierung verwendet, um Operationen, die fehlschlagen könnten,
 * sicher zu modellieren. Konventionell steht {@code Left} für einen Fehler und {@code Right} für einen Erfolg.
 * Dieses Interface ist als "Sealed Interface" implementiert, um die Zustände auf {@link Left} und {@link Right} zu beschränken.</p>
 *
 * @param <L> Der Typ des linken (typischerweise Fehler-) Wertes.
 * @param <R> Der Typ des rechten (typischerweise Erfolgs-) Wertes.
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    /**
     * Repräsentiert den linken Wert, typischerweise den Fehlerfall.
     *
     * @param <L> Der Typ des linken Wertes.
     * @param <R> Der Typ des rechten Wertes (nicht relevant in diesem Fall).
     * @param value der enthaltene Wert.
     */
    record Left<L, R>(L value) implements Either<L, R> {}

    /**
     * Repräsentiert den rechten Wert, typischerweise den Erfolgsfall.
     *
     * @param <L> Der Typ des linken Wertes (nicht relevant in diesem Fall).
     * @param <R> Der Typ des rechten Wertes.
     * @param value der enthaltene Wert.
     */
    record Right<L, R>(R value) implements Either<L, R> {}

    // ---
    // Statische Factory-Methoden

    /**
     * Erstellt eine neue {@code Either}-Instanz im linken Zustand (Fehlerfall).
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> result = Either.left("Ungültige Eingabe");
     * }</pre>
     * </p>
     *
     * @param value der Wert für den linken Zustand.
     * @param <L> der Typ des linken Wertes.
     * @param <R> der Typ des rechten Wertes.
     * @return eine neue {@link Left}-Instanz.
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Erstellt eine neue {@code Either}-Instanz im rechten Zustand (Erfolgsfall).
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> result = Either.right(42);
     * }</pre>
     * </p>
     *
     * @param value der Wert für den rechten Zustand.
     * @param <L> der Typ des linken Wertes.
     * @param <R> der Typ des rechten Wertes.
     * @return eine neue {@link Right}-Instanz.
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    // ---
    // Kernmethoden

    /**
     * Die mächtigste Methode: Wendet eine von zwei Funktionen an, abhängig vom Zustand.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> success = Either.right(10);
     * int result = success.fold(
     * errorMsg -> -1, // Bei Fehler wird -1 zurückgegeben
     * number -> number * 2 // Bei Erfolg wird die Zahl verdoppelt
     * );
     * // result wird 20 sein.
     * }</pre>
     * </p>
     *
     * @param onLeft  die Funktion, die im {@link Left}-Fall angewendet wird.
     * @param onRight die Funktion, die im {@link Right}-Fall angewendet wird.
     * @param <T>     der Rückgabetyp beider Funktionen.
     * @return das Ergebnis der angewendeten Funktion.
     */
    default <T> T fold(Function<L, T> onLeft, Function<R, T> onRight) {
        return switch (this) {
            case Left<L, R>(var left) -> onLeft.apply(left);
            case Right<L, R>(var right) -> onRight.apply(right);
        };
    }

    // ---
    // Transformationen

    /**
     * Transformiert den Wert im rechten (Erfolgs-) Zustand.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> either = Either.right(5);
     * Either<String, String> mapped = either.map(Object::toString);
     * // mapped ist ein Right mit dem Wert "5".
     *
     * Either<String, Integer> error = Either.left("Fehler!");
     * Either<String, String> mappedError = error.map(Object::toString);
     * // mappedError ist ein Left mit dem Wert "Fehler!".
     * }</pre>
     * </p>
     *
     * @param mapper die Funktion, die den rechten Wert transformiert.
     * @param <T> der neue Typ des rechten Wertes.
     * @return eine neue {@code Either}-Instanz mit dem transformierten Wert.
     */
    default <T> Either<L, T> map(Function<R, T> mapper) {
        return fold(Either::left, right -> Either.right(mapper.apply(right)));
    }

    /**
     * Transformiert den Wert im linken (Fehler-) Zustand.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<Integer, String> either = Either.left(500);
     * Either<String, String> mappedLeft = either.mapLeft(code -> "Fehlercode: " + code);
     * // mappedLeft ist ein Left mit dem Wert "Fehlercode: 500".
     * }</pre>
     * </p>
     *
     * @param mapper die Funktion, die den linken Wert transformiert.
     * @param <T> der neue Typ des linken Wertes.
     * @return eine neue {@code Either}-Instanz mit dem transformierten Wert.
     */
    default <T> Either<T, R> mapLeft(Function<L, T> mapper) {
        return fold(left -> Either.left(mapper.apply(left)), Either::right);
    }

    /**
     * Wendet eine Funktion auf den rechten Wert an, die ihrerseits ein {@code Either} zurückgibt.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Function<String, Either<String, Integer>> parseNumber = s -> {
     * try {
     * return Either.right(Integer.parseInt(s));
     * } catch (NumberFormatException e) {
     * return Either.left("Ungültige Zahl: " + s);
     * }
     * };
     *
     * Either<String, String> either = Either.right("123");
     * Either<String, Integer> result = either.flatMap(parseNumber);
     * // result ist ein Right mit dem Wert 123.
     * }</pre>
     * </p>
     *
     * @param mapper die Funktion, die ein neues {@code Either} zurückgibt.
     * @param <T> der neue Typ des rechten Wertes.
     * @return das Ergebnis der Mapper-Funktion.
     */
    default <T> Either<L, T> flatMap(Function<R, Either<L, T>> mapper) {
        return fold(Either::left, mapper);
    }

    // ---
    // Hilfsmethoden

    /**
     * Konvertiert das {@code Either}-Objekt in ein {@link Optional}.
     * Ein {@link Left} wird zu einem leeren {@link Optional}, ein {@link Right} enthält den Wert.
     *
     * @return ein {@link Optional}, das den rechten Wert enthält, oder ein leeres Optional.
     */
    default Optional<R> toOptional() {
        return fold(left -> Optional.empty(), Optional::of);
    }

    /**
     * Gibt den rechten Wert zurück, oder einen Standardwert, wenn das Objekt ein {@link Left} ist.
     *
     * @param defaultValue der Wert, der im Fehlerfall zurückgegeben wird.
     * @return der rechte Wert oder der Standardwert.
     */
    default R getOrElse(R defaultValue) {
        return fold(left -> defaultValue, right -> right);
    }

    /**
     * Gibt den rechten Wert zurück oder löst eine {@link RuntimeException} mit dem linken Wert als Nachricht aus.
     *
     * @return der rechte Wert.
     * @throws RuntimeException wenn das Objekt ein {@link Left} ist.
     */
    default R getOrThrow() {
        return fold(left -> {
            throw new RuntimeException("Either.getOrThrow() called on a Left value: " + left);
        }, right -> right);
    }
}