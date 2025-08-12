package com.example.sandbox.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    // Kernmethoden für die Transformation und Zustandskontrolle

    /**
     * Die mächtigste Methode: Wendet eine von zwei Funktionen an, abhängig vom Zustand.
     * Diese Methode ermöglicht die Transformation eines {@code Either}-Objekts in einen
     * beliebigen Typ {@code T}.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> success = Either.right(10);
     * int result = success.fold(
     * errorMsg -> -1,
     * number -> number * 2
     * );
     * // result wird 20 sein.
     *
     * Either<String, Integer> error = Either.left("Ungültig");
     * int result2 = error.fold(
     * errorMsg -> errorMsg.length(),
     * number -> number * 2
     * );
     * // result2 wird 8 sein.
     * }</pre>
     * </p>
     *
     * @param onLeft die Funktion, die im {@link Left}-Fall angewendet wird.
     * @param onRight die Funktion, die im {@link Right}-Fall angewendet wird.
     * @param <T> der Rückgabetyp beider Funktionen.
     * @return das Ergebnis der angewendeten Funktion.
     */
    default <T> T fold(Function<L, T> onLeft, Function<R, T> onRight) {
        return switch (this) {
            case Left<L, R> left -> onLeft.apply(left.value());
            case Right<L, R> right -> onRight.apply(right.value());
        };
    }

    /**
     * Wendet eine von zwei Funktionen an, um eine Nebenwirkung zu erzeugen, ohne einen Wert zurückzugeben.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> result = Either.left("Fehler!");
     * result.match(
     * error -> System.err.println("Ein Fehler ist aufgetreten: " + error),
     * number -> System.out.println("Ergebnis: " + number)
     * );
     * // Ausgabe: Ein Fehler ist aufgetreten: Fehler!
     * }</pre>
     * </p>
     *
     * @param onLeft die Aktion, die im {@link Left}-Fall ausgeführt wird.
     * @param onRight die Aktion, die im {@link Right}-Fall ausgeführt wird.
     */
    default void match(Consumer<L> onLeft, Consumer<R> onRight) {
        switch (this) {
            case Left<L, R> left -> onLeft.accept(left.value());
            case Right<L, R> right -> onRight.accept(right.value());
        }
    }

    // ---
    // Transformationen

    /**
     * Transformiert den Wert im rechten (Erfolgs-) Zustand.
     * Bleibt ein {@link Left}, wenn das Objekt bereits ein Fehler war.
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
     * @return eine neue {@code Either}-Instanz mit dem transformierten Wert oder dem unveränderten Fehlerwert.
     */
    default <T> Either<L, T> map(Function<R, T> mapper) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.value());
            case Right<L, R> right -> new Right<>(mapper.apply(right.value()));
        };
    }

    /**
     * Transformiert den Wert im linken (Fehler-) Zustand.
     * Bleibt ein {@link Right}, wenn das Objekt bereits ein Erfolg war.
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
     * Transformiert beide Werte des Either-Objekts gleichzeitig.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<Integer, String> either = Either.right("Erfolg!");
     * Either<String, Integer> bimapped = either.bimap(Object::toString, String::length);
     * // bimapped ist ein Right mit dem Wert 7 (Länge von "Erfolg!").
     *
     * Either<Integer, String> error = Either.left(500);
     * Either<String, Integer> bimappedError = error.bimap(code -> "Code: " + code, String::length);
     * // bimappedError ist ein Left mit dem Wert "Code: 500".
     * }</pre>
     * </p>
     *
     * @param leftMapper die Funktion, die den linken Wert transformiert.
     * @param rightMapper die Funktion, die den rechten Wert transformiert.
     * @param <T> der neue Typ des linken Wertes.
     * @param <U> der neue Typ des rechten Wertes.
     * @return eine neue Either-Instanz mit den transformierten Werten.
     */
    default <T, U> Either<T, U> bimap(Function<L, T> leftMapper, Function<R, U> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> Either.left(leftMapper.apply(left.value()));
            case Right<L, R> right -> Either.right(rightMapper.apply(right.value()));
        };
    }

    /**
     * Wendet eine Funktion auf den rechten Wert an, die ihrerseits ein {@code Either} zurückgibt.
     * Dies wird oft für die Verkettung von Operationen verwendet, die fehlschlagen können.
     *
     * <p>Wenn die aktuelle Instanz ein {@link Left} ist, wird der Mapp-Vorgang übersprungen und die
     * bestehende {@link Left}-Instanz zurückgegeben. Andernfalls wird der Mapper angewendet und
     * dessen Ergebnis direkt zurückgegeben.</p>
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
     *
     * Either<String, String> either2 = Either.right("abc");
     * Either<String, Integer> result2 = either2.flatMap(parseNumber);
     * // result2 ist ein Left mit dem Wert "Ungültige Zahl: abc".
     * }</pre>
     * </p>
     *
     * @param mapper die Funktion, die ein neues {@code Either} zurückgibt.
     * @param <T> der neue Typ des rechten Wertes.
     * @return das Ergebnis der Mapper-Funktion.
     */
    default <T> Either<L, T> flatMap(Function<R, Either<L, T>> mapper) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.value());
            case Right<L, R> right -> mapper.apply(right.value());
        };
    }

    // ---
    // Hilfsmethoden und Abfragen

    /**
     * Prüft, ob diese Either-Instanz ein {@link Left} ist.
     *
     * @return true, wenn die Instanz ein Left ist, sonst false.
     */
    default boolean isLeft() {
        return this instanceof Left;
    }

    /**
     * Prüft, ob diese Either-Instanz ein {@link Right} ist.
     *
     * @return true, wenn die Instanz ein Right ist, sonst false.
     */
    default boolean isRight() {
        return this instanceof Right;
    }

    /**
     * Konvertiert das {@code Either}-Objekt in ein {@link Optional}.
     * Ein {@link Left} wird zu einem leeren {@link Optional}, ein {@link Right} enthält den Wert.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Optional<Integer> success = Either.right(42).toOptional();
     * // success enthält den Wert 42.
     * Optional<Integer> error = Either.left("Fehler").toOptional();
     * // error ist leer.
     * }</pre>
     * </p>
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
     * Gibt den rechten Wert zurück oder löst eine benutzerdefinierte RuntimeException mit dem linken Wert aus.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> success = Either.right(42);
     * int value = success.getOrThrow(msg -> new IllegalStateException(msg));
     * // value ist 42.
     *
     * Either<String, Integer> error = Either.left("Zugriff verweigert");
     * // Dies löst eine IllegalStateException mit der Nachricht "Zugriff verweigert" aus.
     * try {
     * error.getOrThrow(IllegalStateException::new);
     * } catch (IllegalStateException e) {
     * System.err.println(e.getMessage());
     * }
     * }</pre>
     * </p>
     *
     * @param exceptionSupplier der Supplier, der die auszulösende Ausnahme liefert.
     * @param <X> der Typ der auszulösenden Ausnahme.
     * @return der rechte Wert.
     * @throws X wenn das Objekt ein {@link Left} ist.
     */
    default <X extends RuntimeException> R getOrThrow(Function<L, X> exceptionSupplier) {
        return fold(left -> {
            throw exceptionSupplier.apply(left);
        }, right -> right);
    }

    /**
     * Filtert den Wert im rechten (Erfolgs-) Zustand basierend auf einem Prädikat.
     *
     * <p>Wenn die {@code Either}-Instanz ein {@link Left} ist, bleibt sie unverändert.
     * Wenn die Instanz ein {@link Right} ist und das Prädikat auf den Wert zutrifft,
     * bleibt sie ebenfalls ein {@link Right}. Trifft das Prädikat jedoch nicht zu,
     * wird die Instanz in einen {@link Left} umgewandelt, der den durch den
     * {@code errorSupplier} bereitgestellten Fehlerwert enthält.</p>
     *
     * <p>Dieses Muster ist besonders nützlich für die Validierung von Werten
     * in einer Verarbeitungskette, wo ein Fehlschlag zu einem spezifischen
     * Fehler führen soll.</p>
     *
     * <p>Beispiel:
     * <pre>{@code
     * // Eine Methode, die eine gerade Zahl erwartet
     * Function<Integer, Either<String, Integer>> checkEven = num ->
     * Either.right(num).filterOrElse(
     * n -> n % 2 == 0,
     * () -> "Die Zahl ist ungerade."
     * );
     *
     * Either<String, Integer> success = checkEven.apply(4);
     * // success ist ein Right mit dem Wert 4.
     *
     * Either<String, Integer> failure = checkEven.apply(5);
     * // failure ist ein Left mit dem Wert "Die Zahl ist ungerade."
     * }</pre>
     * </p>
     *
     * @param predicate die Bedingung, die auf den rechten Wert angewendet wird.
     * @param errorSupplier ein {@link Supplier}, der den Fehlerwert liefert, falls das Prädikat fehlschlägt.
     * @return eine neue {@code Either}-Instanz, entweder ein {@link Right} mit dem ursprünglichen Wert oder ein {@link Left} mit dem neuen Fehlerwert.
     */
    default Either<L, R> filterOrElse(Predicate<R> predicate, Supplier<L> errorSupplier) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.value());
            case Right<L, R> right -> predicate.test(right.value())
                    ? new Right<>(right.value())
                    : new Left<>(errorSupplier.get());
        };
    }

    /**
     * Konvertiert eine Liste von {@code Either}-Instanzen in ein einziges {@code Either}-Objekt.
     *
     * <p>Wenn alle Elemente in der Liste {@link Right} sind, gibt die Methode ein {@code Right} zurück,
     * das eine Liste aller Werte enthält. Wenn mindestens ein Element ein {@link Left} ist,
     * wird die Iteration gestoppt und der erste gefundene {@link Left} zurückgegeben.</p>
     *
     * <p>Beispiel:
     * <pre>{@code
     * List<Either<String, Integer>> successList = List.of(Either.right(1), Either.right(2));
     * Either<String, List<Integer>> success = Either.sequence(successList);
     * // success ist ein Right mit dem Wert [1, 2].
     *
     * List<Either<String, Integer>> failureList = List.of(Either.right(1), Either.left("Fehler!"), Either.right(2));
     * Either<String, List<Integer>> failure = Either.sequence(failureList);
     * // failure ist ein Left mit dem Wert "Fehler!".
     * }</pre>
     * </p>
     *
     * @param eithers die Liste von {@code Either}-Instanzen.
     * @param <L> der Typ des linken (Fehler-) Wertes.
     * @param <R> der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit dem Ergebnis.
     */
    static <L, R> Either<L, List<R>> sequence(List<Either<L, R>> eithers) {
        List<R> results = new ArrayList<>();

        for (Either<L, R> either : eithers) {
            switch (either) {
                case Left<L, R> left -> { return Either.left(left.value()); }
                case Right<L, R> right -> results.add(right.value());
            }
        }

        return Either.right(results);
    }
}