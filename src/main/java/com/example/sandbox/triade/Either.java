package com.example.sandbox.triade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>Repräsentiert einen Wert, der entweder vom Typ {@code L} (Left) oder vom Typ {@code R} (Right) ist.</p>
 *
 * <p>Das Either-Muster wird oft in der funktionalen Programmierung verwendet, um Operationen, die fehlschlagen könnten,
 * sicher zu modellieren. Konventionell steht {@code Left} für einen Fehler und {@code Right} für einen Erfolg.
 * Dieses Interface ist als "Sealed Interface" implementiert, um die Zustände auf {@link Left} und {@link Right} zu beschränken.
 * Es unterstützt die Konzepte des Functors, Applicatives und der Monade.</p>
 *
 * @param <L> Der Typ des linken (typischerweise Fehler-) Wertes.
 * @param <R> Der Typ des rechten (typischerweise Erfolgs-) Wertes.
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    // --- Implementierungen ---

    /**
     * Repräsentiert den linken Wert, typischerweise den Fehlerfall.
     *
     * @param <L>   Der Typ des linken Wertes.
     * @param <R>   Der Typ des rechten Wertes (nicht relevant in diesem Fall).
     * @param value der enthaltene Wert.
     */
    record Left<L, R>(L value) implements Either<L, R> {
    }

    /**
     * Repräsentiert den rechten Wert, typischerweise den Erfolgsfall.
     *
     * @param <L>   Der Typ des linken Wertes (nicht relevant in diesem Fall).
     * @param <R>   Der Typ des rechten Wertes.
     * @param value der enthaltene Wert.
     */
    record Right<L, R>(R value) implements Either<L, R> {
    }

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
     * @param <L>   der Typ des linken Wertes.
     * @param <R>   der Typ des rechten Wertes.
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
     * @param <L>   der Typ des linken Wertes.
     * @param <R>   der Typ des rechten Wertes.
     * @return eine neue {@link Right}-Instanz.
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    // --- Applicative: pure ---

    /**
     * Erstellt eine neue {@code Either}-Instanz im rechten Zustand (Erfolgsfall).
     * Dies ist die "pure" Funktion aus der Applicative-Typklasse.
     *
     * @param value der Wert für den rechten Zustand.
     * @param <L>   der Typ des linken Wertes.
     * @param <R>   der Typ des rechten Wertes.
     * @return eine neue {@link Right}-Instanz.
     */
    static <L, R> Either<L, R> pure(R value) {
        return Either.right(value);
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
     *     errorMsg -> -1,
     *     number -> number * 2
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
    default <T> T fold(Function<? super L, ? extends T> onLeft,
                       Function<? super R, ? extends T> onRight) {
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
     *     error -> System.err.println("Ein Fehler ist aufgetreten: " + error),
     *     number -> System.out.println("Ergebnis: " + number)
     * );
     * // Ausgabe: Ein Fehler ist aufgetreten: Fehler!
     * }</pre>
     * </p>
     *
     * @param onLeft  die Aktion, die im {@link Left}-Fall ausgeführt wird.
     * @param onRight die Aktion, die im {@link Right}-Fall ausgeführt wird.
     */
    default void match(Consumer<? super L> onLeft, Consumer<? super R> onRight) {
        switch (this) {
            case Left<L, R> left -> onLeft.accept(left.value());
            case Right<L, R> right -> onRight.accept(right.value());
        }
    }

    // ---
    // Transformationen

    /**
     * Transformiert den Wert im rechten (Erfolgs-) Zustand (Functor-Operation).
     * Bleibt ein {@link Left}, wenn das Objekt bereits ein Fehler war.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> either = Either.right(5);
     * Either<String, String> mapped = either.map(Object::toString);
     * // mapped ist ein Right mit dem Wert "5".
     * }</pre>
     * </p>
     *
     * @param mapper die Funktion, die den rechten Wert transformiert.
     * @param <T>    der neue Typ des rechten Wertes.
     * @return eine neue {@code Either}-Instanz mit dem transformierten Wert oder dem unveränderten Fehlerwert.
     */
    @SuppressWarnings("unchecked")
    default <T> Either<L, T> map(Function<? super R, ? extends T> mapper) {
        return switch (this) {
            case Left<L, R> left -> (Either<L, T>) this;
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
     * @param <T>    der neue Typ des linken Wertes.
     * @return eine neue {@code Either}-Instanz mit dem transformierten Wert.
     */
    default <T> Either<T, R> mapLeft(Function<? super L, ? extends T> mapper) {
        return fold(left -> Either.left(mapper.apply(left)), Either::right);
    }

    /**
     * Transformiert beide Werte des Either-Objekts gleichzeitig (BiFunctor-Operation).
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<Integer, String> either = Either.right("Erfolg!");
     * Either<String, Integer> bimapped = either.bimap(Object::toString, String::length);
     * // bimapped ist ein Right mit dem Wert 7 (Länge von "Erfolg!").
     * }</pre>
     * </p>
     *
     * @param leftMapper  die Funktion, die den linken Wert transformiert.
     * @param rightMapper die Funktion, die den rechten Wert transformiert.
     * @param <T>         der neue Typ des linken Wertes.
     * @param <U>         der neue Typ des rechten Wertes.
     * @return eine neue Either-Instanz mit den transformierten Werten.
     */
    default <T, U> Either<T, U> bimap(Function<? super L, ? extends T> leftMapper,
                                      Function<? super R, ? extends U> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> Either.left(leftMapper.apply(left.value()));
            case Right<L, R> right -> Either.right(rightMapper.apply(right.value()));
        };
    }

    /**
     * Wendet eine Funktion auf den rechten Wert an, die ihrerseits ein
     * {@code Either} zurückgibt (Monaden-Operation). Dies wird oft für die
     * Verkettung von Operationen verwendet, die fehlschlagen können.
     *
     * <p>Wenn die aktuelle Instanz ein {@link Left} ist, wird der Mapp-Vorgang
     * übersprungen und die bestehende {@link Left}-Instanz zurückgegeben.
     * Andernfalls wird der Mapper angewendet und dessen Ergebnis direkt
     * zurückgegeben.</p>
     *
     * @param mapper die Funktion, die ein neues {@code Either} zurückgibt.
     * @param <T>    der neue Typ des rechten Wertes.
     * @return das Ergebnis der Mapper-Funktion.
     */
    @SuppressWarnings("unchecked")
    default <T> Either<L, T> flatMap(Function<? super R, Either<L, T>> mapper) {
        return switch (this) {
            case Left<L, R> left -> (Either<L, T>) this;
            case Right<L, R> right -> mapper.apply(right.value());
        };
    }

    // --- Applicative: ap ---

    /**
     * Wendet eine im {@code Either} gekapselte Funktion auf den Wert dieses
     * {@code Either} an (Applicative-Operation). Dies ermöglicht die Anwendung
     * einer Funktion auf einen Wert, wobei beide in einem {@code Either}
     * gekapselt sind.
     *
     * <p>Wenn die Funktion oder dieses {@code Either} ein {@link Left} ist,
     * wird der Fehlerwert zurückgegeben. Bei {@link Left} wird der Fehler des
     * Funktions-Eithers bevorzugt, wenn beide {@link Left} sind, was aber
     * aufgrund des Typs {@code Either<L, T>} nicht eintreten kann, da nur der
     * Fehler des {@code fn} Eithers den Typ {@code L} hat.</p>
     * <p>Beispiel:
     * <pre>{@code
     * // 1. Functor-Map-Anwendung
     * Function<Integer, String> intToString = i -> "Result: " + i;
     * Either<String, Function<Integer, String>> eitherFn = Either.right(intToString);
     * Either<String, Integer> eitherValue = Either.right(42);
     * Either<String, String> result = eitherValue.ap(eitherFn);
     * // result ist ein Right mit dem Wert "Result: 42".
     *
     * // ----------------------------------------------------\ 
     *
     * // 2. Applicative Functor für die Konstruktion komplexer Objekte (Currying):
     * // Angenommen, es gibt:
     * // record User(String id, String name, int age) {}
     * // Either<String, String> idState = Either.right("101");
     * // Either<String, String> nameState = Either.right("Alice");
     * // Either<String, Integer> ageState = Either.right(30);
     *
     * Either<String, User> userEither =
     * // Hebt den currierten Konstruktor in den Either-Kontext (mit Either.pure)
     * Either.pure(id -> name -> age -> new User(id, name, age))
     *     .ap(idState)   // Wendet den ersten Either-Wert an
     *     .ap(nameState) // Wendet den zweiten Either-Wert an
     *     .ap(ageState); // Wendet den dritten Either-Wert an
     *
     * // Wenn alle States Right sind, ist userEither ein Right<User>.
     * // Wenn mindestens ein State Left ist, ist userEither ein Left<String> mit dem ersten Fehler.
     * }</pre>
     * </p>
     *
     * @param fn  ein {@code Either}, das die auf den rechten Wert anzuwendende Funktion enthält.
     * @param <T> der neue Typ des rechten Wertes (der Rückgabetyp der Funktion).
     * @return ein neues {@code Either} mit dem angewendeten Ergebnis oder dem Fehler.
     */
    default <T> Either<L, T> ap(Either<L, Function<R, T>> fn) {
        return fn.flatMap(this::map);
    }

    // --- Symmetrie ---

    /**
     * Tauscht die linke (Fehler-) und die rechte (Erfolgs-) Seite des {@code Either}-Objekts.
     * Ein {@link Left} wird zu einem {@link Right} und umgekehrt.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Either<String, Integer> success = Either.right(42);
     * Either<Integer, String> swappedSuccess = success.swap();
     * // swappedSuccess ist ein Left mit dem Wert 42.
     * }</pre>
     * </p>
     *
     * @return eine neue {@code Either}-Instanz mit vertauschten Typen.
     */
    default Either<R, L> swap() {
        return fold(Either::right, Either::left);
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

    // --- Optionals & Streams ---

    /**
     * Konvertiert das {@code Either}-Objekt in ein {@link Optional}.
     * Ein {@link Left} wird zu einem leeren {@link Optional}, ein {@link Right} enthält den Wert.
     *
     * @return ein {@link Optional}, das den rechten Wert enthält, oder ein leeres Optional.
     */
    default Optional<R> toOptional() {
        return fold(l -> Optional.empty(), Optional::of);
    }

    /**
     * Konvertiert das {@code Either}-Objekt in ein {@link Optional}, fokussiert auf den linken Wert.
     * Ein {@link Right} wird zu einem leeren {@link Optional}, ein {@link Left} enthält den Wert.
     *
     * @return ein {@link Optional}, das den linken Wert enthält, oder ein leeres Optional.
     */
    default Optional<L> toOptionalLeft() {
        return fold(Optional::of, r -> Optional.empty());
    }

    /**
     * Konvertiert den rechten Wert des {@code Either}-Objekts in einen {@link Stream}.
     * Ein {@link Left} wird zu einem leeren {@link Stream}, ein {@link Right} enthält den Wert als einzigen Stream-Element.
     *
     * @return ein {@link Stream} mit dem rechten Wert, oder ein leerer Stream.
     */
    default Stream<R> stream() {
        return fold(l -> Stream.empty(), Stream::of);
    }

    // --- Zugriff mit Defaults ---

    /**
     * Gibt den rechten Wert zurück, oder einen Standardwert, wenn das Objekt ein {@link Left} ist.
     *
     * @param defaultValue der Wert, der im Fehlerfall zurückgegeben wird.
     * @return der rechte Wert oder der Standardwert.
     */
    default R getOrElse(R defaultValue) {
        return fold(l -> defaultValue, r -> r);
    }

    /**
     * Gibt den rechten Wert zurück oder ruft einen {@link Supplier} auf, um einen Standardwert zu erhalten,
     * wenn das Objekt ein {@link Left} ist.
     *
     * @param supplier der {@link Supplier}, der den Standardwert liefert, falls ein Fehler vorliegt.
     * @return der rechte Wert oder der durch den Supplier gelieferte Wert.
     */
    default R orElseGet(Supplier<? extends R> supplier) {
        return fold(l -> supplier.get(), r -> r);
    }

    /**
     * Gibt den rechten Wert zurück oder löst eine benutzerdefinierte RuntimeException mit dem linken Wert aus.
     *
     * @param exceptionSupplier der Supplier, der die auszulösende Ausnahme liefert, basierend auf dem linken Wert.
     * @param <X>               der Typ der auszulösenden Ausnahme.
     * @return der rechte Wert.
     * @throws X wenn das Objekt ein {@link Left} ist.
     */
    default <X extends RuntimeException> R getOrThrow(Function<? super L, X> exceptionSupplier) {
        return fold(l -> {
            throw exceptionSupplier.apply(l);
        }, r -> r);
    }

    // --- Peeking ---

    /**
     * Führt eine Nebenwirkung mit dem rechten Wert aus, falls vorhanden, und gibt das {@code Either} unverändert zurück.
     * Dies ist nützlich für das "Hineinschauen" in den Erfolgsfall einer Kette von Operationen (z.B. zum Logging).
     *
     * @param action die Aktion, die auf den rechten Wert angewendet wird.
     * @return diese {@code Either}-Instanz.
     */
    default Either<L, R> peek(Consumer<? super R> action) {
        match(l -> {}, action);
        return this;
    }

    /**
     * Führt eine Nebenwirkung mit dem linken Wert aus, falls vorhanden, und gibt das {@code Either} unverändert zurück.
     * Dies ist nützlich für das "Hineinschauen" in den Fehlerfall einer Kette von Operationen.
     *
     * @param action die Aktion, die auf den linken Wert angewendet wird.
     * @return diese {@code Either}-Instanz.
     */
    default Either<L, R> peekLeft(Consumer<? super L> action) {
        match(action, r -> {});
        return this;
    }

    // --- Filter ---

    /**
     * Filtert den Wert im rechten (Erfolgs-) Zustand basierend auf einem Prädikat.
     *
     * <p>Wenn die Instanz ein {@link Right} ist und das Prädikat nicht zutrifft,
     * wird die Instanz in einen {@link Left} umgewandelt, der den durch den
     * {@code errorSupplier} bereitgestellten Fehlerwert enthält.</p>
     *
     * @param predicate     die Bedingung, die auf den rechten Wert angewendet wird.
     * @param errorSupplier ein {@link Supplier}, der den Fehlerwert liefert, falls das Prädikat fehlschlägt.
     * @return eine neue {@code Either}-Instanz, entweder ein {@link Right} mit dem ursprünglichen Wert oder ein {@link Left} mit dem neuen Fehlerwert.
     */
    default Either<L, R> filterOrElse(Predicate<? super R> predicate,
                                      Supplier<? extends L> errorSupplier) {
        return switch (this) {
            case Left<L, R> left -> this;
            case Right<L, R> right -> predicate.test(right.value())
                    ? this // Wir geben this zurück, da der Wert unverändert bleibt
                    : new Left<>(errorSupplier.get());
        };
    }

    // --- Sequenzierung ---

    /**
     * Konvertiert eine Liste von {@code Either}-Instanzen in ein einziges {@code Either}-Objekt.
     *
     * <p>Wenn alle Elemente in der Liste {@link Right} sind, gibt die Methode ein {@code Right} zurück,
     * das eine Liste aller Werte enthält. Wenn mindestens ein Element ein {@link Left} ist,
     * wird die Iteration gestoppt und der <strong>erste gefundene {@link Left}</strong> zurückgegeben
     * ("Fail-Fast"-Verhalten).</p>
     *
     * @param eithers die Liste von {@code Either}-Instanzen.
     * @param <L>     der Typ des linken (Fehler-) Wertes.
     * @param <R>     der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit dem Ergebnis.
     */
    static <L, R> Either<L, List<R>> sequence(List<Either<L, R>> eithers) {
        List<R> results = new ArrayList<>();

        for (Either<L, R> either : eithers) {
            switch (either) {
                case Left<L, R> left -> {
                    return Either.left(left.value());
                }
                case Right<L, R> right -> results.add(right.value());
            }
        }

        return Either.right(results);
    }

    /**
     * Konvertiert eine Liste von {@code Either}-Instanzen in ein einziges {@code Either}-Objekt und
     * sammelt alle Fehler, anstatt beim ersten Fehler abzubrechen ("Fail-Slow"-Verhalten).
     *
     * <p>Wenn alle Elemente {@link Right} sind, gibt die Methode ein {@code Right} mit einer Liste aller
     * Werte zurück. Wenn ein oder mehrere Elemente {@link Left} sind, gibt sie ein {@code Left} mit einer
     * Liste <strong>aller Fehler</strong> zurück.
     *
     * @param eithers die Liste von {@code Either}-Instanzen.
     * @param <L>     der Typ des linken (Fehler-) Wertes.
     * @param <R>     der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit einer Liste von Fehlern (Left) oder einer Liste von Ergebnissen (Right).
     */
    static <L, R> Either<List<L>, List<R>> sequenceAccumulate(List<Either<L, R>> eithers) {
        List<L> errors = new ArrayList<>();
        List<R> results = new ArrayList<>();
        eithers.forEach(either -> either.match(errors::add, results::add));
        return errors.isEmpty() ? Either.right(results) : Either.left(errors);
    }

    // --- Try/Catch Integration ---

    /**
     * Führt eine Operation aus, die eine Ausnahme auslösen könnte, und kapselt das Ergebnis
     * in ein {@code Either}, wobei die geworfene {@link Throwable} direkt als linker Wert
     * zurückgegeben wird. Dies ist nützlich für Fälle, in denen der vollständige Stacktrace
     * oder die ursprüngliche Ausnahme beibehalten werden soll.
     *
     * @param supplier der {@link ThrowingSupplier}, der die risikoreiche Operation ausführt.
     * @param <R>      der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit dem Ergebnis oder der geworfenen Ausnahme.
     */
    static <R> Either<Throwable, R> catching(ThrowingSupplier<? extends R> supplier) {
        try {
            return Either.right(supplier.get());
        } catch (Throwable t) {
            return Either.left(t);
        }
    }

    /**
     * Führt eine Operation aus, die eine Ausnahme auslösen könnte, und kapselt das Ergebnis
     * in ein {@code Either}. Eine erfolgreiche Ausführung führt zu einem {@link Right},
     * während eine geworfene {@link Throwable} mit einem {@code errorMapper} in den
     * linken Fehlertyp {@code L} abgebildet wird. Dies ermöglicht eine gezielte
     * Fehlerbehandlung und Typisierung.
     *
     * @param supplier    der {@link ThrowingSupplier}, der die risikoreiche Operation ausführt.
     * @param errorMapper die Funktion, die eine geworfene {@link Throwable} in den linken Fehlertyp {@code L} abbildet.
     * @param <L>         der Typ des linken (Fehler-) Wertes.
     * @param <R>         der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit dem Ergebnis oder dem abgebildeten Fehler.
     */
    static <L, R> Either<L, R> catching(ThrowingSupplier<? extends R> supplier,
                                        Function<? super Throwable, ? extends L> errorMapper) {
        try {
            return Either.right(supplier.get());
        } catch (Throwable t) {
            return Either.left(errorMapper.apply(t));
        }
    }

    /**
     * Wendet eine Funktion {@code fn} auf einen Eingabewert an und fängt dabei auftretende
     * Ausnahmen ab, um das Ergebnis als {@code Either} zurückzugeben. Die geworfene {@link Throwable}
     * wird mit einem {@code errorMapper} in den linken Fehlertyp {@code L} abgebildet.
     * Diese Methode ist praktisch für das sichere "Lifting" von Funktionen, die Ausnahmen werfen können.
     *
     * @param value       der Eingabewert, auf den die Funktion angewendet wird.
     * @param fn          die Funktion, die auf den Wert angewendet wird und möglicherweise eine Ausnahme auslöst.
     * @param errorMapper die Funktion, die eine geworfene {@link Throwable} in den linken Fehlertyp {@code L} abbildet.
     * @param <L>         der Typ des linken (Fehler-) Wertes.
     * @param <T>         der Typ des Eingabewertes.
     * @param <R>         der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit dem Ergebnis oder dem abgebildeten Fehler.
     */
    static <L, T, R> Either<L, R> catching(T value,
                                           ThrowingFunction<? super T, ? extends R> fn,
                                           Function<? super Throwable, ? extends L> errorMapper) {
        try {
            return Either.right(fn.apply(value));
        } catch (Throwable t) {
            return Either.left(errorMapper.apply(t));
        }
    }

    /**
     * Wendet eine Funktion {@code fn} auf einen Eingabewert an und fängt dabei auftretende
     * Ausnahmen ab, um das Ergebnis als {@code Either} zurückzugeben. Die geworfene {@link Throwable}
     * wird dabei direkt als linker Wert zurückgegeben, um den vollständigen Stacktrace zu erhalten.
     * Diese Methode ist nützlich, wenn die Fehlerbehandlung zentralisiert und nicht
     * sofort umgewandelt werden soll.
     *
     * @param value der Eingabewert, auf den die Funktion angewendet wird.
     * @param fn    die Funktion, die auf den Wert angewendet wird und möglicherweise eine Ausnahme auslöst.
     * @param <T>   der Typ des Eingabewertes.
     * @param <R>   der Typ des rechten (Erfolgs-) Wertes.
     * @return ein {@code Either} mit dem Ergebnis oder der geworfenen Ausnahme.
     */
    static <T, R> Either<Throwable, R> catchingValue(T value,
                                           ThrowingFunction<? super T, ? extends R> fn) {
        try {
            return Either.right(fn.apply(value));
        } catch (Throwable t) {
            return Either.left(t);
        }
    }

    /**
     * Führt die gegebene Funktion auf dem Erfolgswert dieses {@code Either} aus und
     * wandelt das Ergebnis in einen neuen {@code Either}-Typ um.
     * <p>
     * Diese Methode ist ein funktionaler 'Bind'- oder 'flatMap'-Operator, der es ermöglicht, 
     * eine Kette von Operationen aufzubauen, die Ausnahmen auslösen können. Wenn die
     * übergebene Funktion eine {@link Throwable}-Ausnahme wirft, wird diese abgefangen
     * und mithilfe des {@code errorMapper} in den linken (Fehler-) Wert umgewandelt.
     * <p>
     * Ist dieses {@code Either} bereits ein Fehler ({@link Left}), wird die Funktion nicht
     * ausgeführt, und der bestehende Fehler wird einfach weitergegeben.
     *
     * @param fn            die Funktion, die auf den Wert angewendet wird und eine Ausnahme auslösen kann.
     * Die Funktion muss einen Wert vom Typ {@code T} zurückgeben.
     * @param errorMapper   eine Funktion, die eine geworfene {@link Throwable}-Ausnahme
     * in einen Wert vom Typ {@code L} umwandelt.
     * @param <T>           der Typ des Erfolgs-Rückgabewertes der Funktion.
     * @return ein neues {@code Either} mit dem Ergebnis der Operation im Erfolgsfall
     * oder dem umgewandelten Fehler im Ausnahmefall.
     */
    default <T> Either<L, T> catching(ThrowingFunction<R, T> fn,
                                      Function<? super Throwable, ? extends L> errorMapper) {
        return flatMap(r -> {
            try {
                return Either.right(fn.apply(r));
            } catch (Throwable t) {
                return Either.left(errorMapper.apply(t));
            }
        });
    }

    // --- Funktion "liften" ---

    /**
     * Erstellt eine Funktion, die eine normale Funktion von {@code R} nach {@code T} nimmt und sie zu einer
     * Funktion von {@code Either<L, R>} nach {@code Either<L, T>} "hebt" (liftet).
     *
     * <p>Das Lifting erlaubt es, eine Funktion auf den Wert innerhalb des {@code Either} anzuwenden,
     * ohne die gesamte Struktur neu schreiben zu müssen (Currying des Functor-Maps).</p>
     *
     * @param f   die Funktion von {@code R} nach {@code T}.
     * @param <L> der Typ des linken (Fehler-) Wertes, der unverändert bleibt.
     * @param <R> der ursprüngliche Typ des rechten Wertes.
     * @param <T> der neue Typ des rechten Wertes.
     * @return eine gehobene Funktion.
     */
    static <L, R, T> Function<Either<L, R>, Either<L, T>> lift(Function<? super R, ? extends T> f) {
        return either -> either.map(f);
    }
}
