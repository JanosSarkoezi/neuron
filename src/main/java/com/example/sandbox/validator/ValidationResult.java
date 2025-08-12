package com.example.sandbox.validator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.ArrayList;

/**
 * <p>Repräsentiert das Ergebnis einer Validierung, das entweder erfolgreich ist (mit einem Wert)
 * oder fehlschlägt (mit einer Liste von Fehlermeldungen).</p>
 *
 * <p>Diese Klasse implementiert das "Sealed Interface"-Muster, um sicherzustellen, dass es nur
 * zwei mögliche Untertypen gibt: {@link Success} und {@link Failure}. Dadurch wird die
 * Handhabung von Validierungsergebnissen sicherer und ausdrucksstärker,
 * insbesondere in Verbindung mit Java's Switch-Expressions.</p>
 *
 * @param <T> der Typ des Wertes im Erfolgsfall.
 */
public sealed interface ValidationResult<T> permits ValidationResult.Success, ValidationResult.Failure {

    /**
     * Repräsentiert ein erfolgreiches Validierungsergebnis.
     *
     * @param <T> der Typ des enthaltenen Wertes.
     * @param value der erfolgreich validierte Wert.
     */
    record Success<T>(T value) implements ValidationResult<T> {
        public Success {
            Objects.requireNonNull(value, "Success value cannot be null");
        }
    }

    /**
     * Repräsentiert ein fehlgeschlagenes Validierungsergebnis mit einer Liste von Fehlermeldungen.
     *
     * @param <T> der Typ, der im Erfolgsfall erwartet worden wäre.
     * @param errors eine Liste von Strings, die die Fehlermeldungen enthalten.
     */
    record Failure<T>(List<String> errors) implements ValidationResult<T> {
        public Failure {
            Objects.requireNonNull(errors, "Errors cannot be null");
            if (errors.isEmpty()) {
                throw new IllegalArgumentException("Failure must have at least one error");
            }
        }
    }

    // ---

    /**
     * Factory-Methode zur Erstellung eines erfolgreichen {@link ValidationResult}.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<String> result = ValidationResult.success("Hallo Welt");
     * }
     * </pre>
     * </p>
     *
     * @param value der Wert, der erfolgreich validiert wurde.
     * @param <T> der Typ des Wertes.
     * @return ein neues {@link Success} Objekt.
     */
    static <T> ValidationResult<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Factory-Methode zur Erstellung eines fehlgeschlagenen {@link ValidationResult} mit einer einzelnen Nachricht.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<Integer> result = ValidationResult.failure("Die Eingabe war ungültig.");
     * }
     * </pre>
     * </p>
     *
     * @param message die Fehlermeldung.
     * @param <T> der Typ, der im Erfolgsfall erwartet worden wäre.
     * @return ein neues {@link Failure} Objekt mit einer Liste, die die Nachricht enthält.
     */
    static <T> ValidationResult<T> failure(String message) {
        return new Failure<>(List.of(message));
    }

    /**
     * Factory-Methode zur Erstellung eines fehlgeschlagenen {@link ValidationResult} mit einer Liste von Nachrichten.
     *
     * <p>Beispiel:
     * <pre>{@code
     * List<String> fehler = List.of("Name fehlt", "E-Mail-Format ungültig");
     * ValidationResult<User> result = ValidationResult.failure(fehler);
     * }
     * </pre>
     * </p>
     *
     * @param errors die Liste der Fehlermeldungen.
     * @param <T> der Typ, der im Erfolgsfall erwartet worden wäre.
     * @return ein neues {@link Failure} Objekt.
     */
    static <T> ValidationResult<T> failure(List<String> errors) {
        return new Failure<>(errors);
    }

    // ---

    /**
     * Überprüft, ob das Validierungsergebnis erfolgreich ist.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<String> successResult = ValidationResult.success("ok");
     * System.out.println(successResult.isValid()); // true
     *
     * ValidationResult<String> failureResult = ValidationResult.failure("Fehler");
     * System.out.println(failureResult.isValid()); // false
     * }
     * </pre>
     * </p>
     *
     * @return {@code true}, wenn es sich um eine {@link Success}-Instanz handelt, sonst {@code false}.
     */
    default boolean isValid() {
        return this instanceof Success;
    }

    /**
     * Gibt den Wert im Erfolgsfall zurück, andernfalls einen Standardwert.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<Integer> result = ValidationResult.failure("Keine Zahl gefunden");
     * int value = result.getOrElse(-1); // value wird -1 sein
     * }
     * </pre>
     * </p>
     *
     * @param defaultValue der Wert, der im Fehlerfall zurückgegeben wird.
     * @return der validierte Wert oder der Standardwert.
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Failure<T>(var errors) -> defaultValue;
        };
    }

    /**
     * Konvertiert das Ergebnis in ein {@link Optional}.
     *
     * <p>Beispiel:
     * <pre>{@code
     * // Erfolgreicher Fall
     * ValidationResult<String> successResult = ValidationResult.success("Daten");
     * Optional<String> optionalValue = successResult.toOptional(); // Optional["Daten"]
     *
     * // Fehlerfall
     * ValidationResult<Integer> failureResult = ValidationResult.failure("Eingabe ungültig");
     * Optional<Integer> optionalEmpty = failureResult.toOptional(); // Optional.empty
     * }
     * </pre>
     * </p>
     *
     * @return ein {@link Optional}, das den Wert enthält, wenn das Ergebnis erfolgreich war,
     * ansonsten ein leeres {@link Optional}.
     */
    default Optional<T> toOptional() {
        return switch (this) {
            case Success<T>(var value) -> Optional.of(value);
            case Failure<T>(var errors) -> Optional.empty();
        };
    }

    // ---

    /**
     * Transformiert den Wert eines erfolgreichen Ergebnisses.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<String> result = ValidationResult.success("123");
     * ValidationResult<Integer> mappedResult = result.map(Integer::parseInt);
     * // mappedResult ist ein Success<Integer> mit dem Wert 123
     * }
     * </pre>
     * </p>
     *
     * @param mapper eine Funktion, die den Wert transformiert.
     * @param <U> der Typ des neuen Wertes.
     * @return ein neues {@link ValidationResult} mit dem transformierten Wert oder
     * die ursprüngliche {@link Failure}-Instanz.
     */
    default <U> ValidationResult<U> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T>(var value) -> success(mapper.apply(value));
            case Failure<T>(var errors) -> new Failure<>(errors);
        };
    }

    /**
     * Transformiert den Wert eines erfolgreichen Ergebnisses in ein neues {@link ValidationResult}.
     *
     * <p>Beispiel:
     * <pre>{@code
     * // Angenommen, diese Methode validiert, ob eine Zahl positiv ist
     * Function<Integer, ValidationResult<Integer>> validatePositive = x -> {
     * if (x > 0) return ValidationResult.success(x);
     * return ValidationResult.failure("Zahl muss positiv sein.");
     * };
     *
     * ValidationResult<Integer> result = ValidationResult.success(10)
     * .flatMap(validatePositive);
     * // result ist ein Success<Integer> mit Wert 10
     *
     * ValidationResult<Integer> negativeResult = ValidationResult.success(-5)
     * .flatMap(validatePositive);
     * // negativeResult ist ein Failure<Integer>
     * }
     * </pre>
     * </p>
     *
     * @param mapper eine Funktion, die den Wert in ein neues {@link ValidationResult} umwandelt.
     * @param <U> der Typ des neuen Wertes.
     * @return das Ergebnis der Mapper-Funktion oder die ursprüngliche {@link Failure}-Instanz.
     */
    default <U> ValidationResult<U> flatMap(Function<T, ValidationResult<U>> mapper) {
        return switch (this) {
            case Success<T>(var value) -> mapper.apply(value);
            case Failure<T>(var errors) -> new Failure<>(errors);
        };
    }

    // ---

    /**
     * Wendet je nach Ergebnis eine von zwei Funktionen an (entweder bei Erfolg oder bei Misserfolg).
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<String> result = ValidationResult.success("Hallo");
     * String output = result.fold(
     * s -> "Erfolgreich: " + s,
     * errors -> "Fehler: " + String.join(", ", errors)
     * );
     * // output wird "Erfolgreich: Hallo" sein
     * }
     * </pre>
     * </p>
     *
     * @param onSuccess die Funktion, die im Erfolgsfall angewendet wird.
     * @param onFailure die Funktion, die im Fehlerfall angewendet wird.
     * @param <R> der Rückgabetyp der Funktionen.
     * @return das Ergebnis der angewendeten Funktion.
     */
    default <R> R fold(
            Function<T, R> onSuccess,
            Function<List<String>, R> onFailure
    ) {
        return switch (this) {
            case Success<T>(var value) -> onSuccess.apply(value);
            case Failure<T>(var errors) -> onFailure.apply(errors);
        };
    }

    /**
     * Führt eine Aktion aus, wenn das Ergebnis erfolgreich ist ({@link Success}),
     * und gibt das ursprüngliche {@link ValidationResult} zurück, sodass weitere
     * Operationen in einer Methodenkette möglich sind.
     *
     * <p>Beispiel:</p>
     * <pre>{@code
     * ValidationResult<String> result = ValidationResult.success("Hallo Welt")
     *     .onSuccess(value -> System.out.println("Validiert: " + value))
     *     .map(String::toUpperCase);
     *
     * // Ausgabe: "Validiert: Hallo Welt"
     * // result ist ein Success mit Wert "HALLO WELT"
     * }</pre>
     *
     * @param action eine Aktion, die auf den Wert angewendet wird, wenn das Ergebnis erfolgreich ist
     * @return die ursprüngliche {@link ValidationResult}-Instanz (für chaining)
     */
    default ValidationResult<T> onSuccess(Consumer<T> action) {
        if (this instanceof Success<T>(var value)) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Führt eine Aktion aus, wenn das Ergebnis fehlgeschlagen ist ({@link Failure}),
     * und gibt das ursprüngliche {@link ValidationResult} zurück, sodass weitere
     * Operationen in einer Methodenkette möglich sind.
     *
     * <p>Beispiel:</p>
     * <pre>{@code
     * ValidationResult<Integer> result = ValidationResult.failure("Keine Zahl gefunden")
     *     .onFailure(errors -> System.err.println("Fehler: " + errors))
     *     .getOrElse(-1);
     *
     * // Ausgabe: "Fehler: [Keine Zahl gefunden]"
     * // result ist -1 (durch getOrElse)
     * }</pre>
     *
     * @param action eine Aktion, die auf die Liste der Fehlermeldungen angewendet wird,
     *               wenn das Ergebnis fehlgeschlagen ist
     * @return die ursprüngliche {@link ValidationResult}-Instanz (für chaining)
     */
    default ValidationResult<T> onFailure(Consumer<List<String>> action) {
        if (this instanceof Failure<T>(var errors)) {
            action.accept(errors);
        }
        return this;
    }

    /**
     * Führt je nach Ergebnis entweder eine Aktion im Erfolgsfall ({@link Success}) oder
     * eine andere Aktion im Fehlerfall ({@link Failure}) aus.
     * Gibt das ursprüngliche {@link ValidationResult} zurück, um Methodenketten zu ermöglichen.
     *
     * <p>Beispiel:</p>
     * <pre>{@code
     * ValidationResult<String> result = ValidationResult.failure("Ungültige Eingabe")
     *     .ifSuccessOrElse(
     *         value -> System.out.println("Validiert: " + value),
     *         errors -> System.err.println("Fehler: " + errors)
     *     )
     *     .map(String::toLowerCase);
     *
     * // Ausgabe: "Fehler: [Ungültige Eingabe]"
     * // result ist ein Failure (map wird nicht angewendet)
     * }</pre>
     *
     * @param successAction eine Aktion, die auf den Wert angewendet wird, wenn das Ergebnis erfolgreich ist
     * @param errorAction   eine Aktion, die auf die Liste der Fehlermeldungen angewendet wird,
     *                      wenn das Ergebnis fehlgeschlagen ist
     * @return die ursprüngliche {@link ValidationResult}-Instanz (für chaining)
     */
    default ValidationResult<T> ifSuccessOrElse(Consumer<T> successAction, Consumer<List<String>> errorAction) {
        switch (this) {
            case Success<T>(var value) -> successAction.accept(value);
            case Failure<T>(var errors) -> errorAction.accept(errors);
        }
        return this;
    }


    /**
     * Kombiniert dieses Ergebnis mit einem anderen.
     * Wenn beide erfolgreich sind, bleibt das erste Ergebnis bestehen.
     * Wenn eines fehlschlägt, wird das fehlgeschlagene Ergebnis zurückgegeben.
     * Wenn beide fehlschlagen, werden die Fehlermeldungen kombiniert.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<User> result1 = ValidationResult.failure("Benutzername fehlt");
     * ValidationResult<User> result2 = ValidationResult.failure("Passwort ist zu kurz");
     *
     * ValidationResult<User> combined = result1.combine(result2);
     * // combined enthält beide Fehlermeldungen in seiner Liste.
     * }
     * </pre>
     * </p>
     *
     * @param other das andere {@link ValidationResult} zum Kombinieren.
     * @return das kombinierte {@link ValidationResult}.
     */
    default ValidationResult<T> combine(ValidationResult<T> other) {
        return switch (this) {
            case Success<T>(var value1) -> switch (other) {
                case Success<T>(var value2) -> this;
                case Failure<T>(var errors2) -> other;
            };
            case Failure<T>(var errors1) -> switch (other) {
                case Success<T>(var value2) -> this;
                case Failure<T>(var errors2) -> {
                    var combinedErrors = new ArrayList<String>();
                    combinedErrors.addAll(errors1);
                    combinedErrors.addAll(errors2);
                    yield new Failure<>(combinedErrors);
                }
            };
        };
    }

    // ---

    /**
     * Führt eine Operation im Erfolgs- oder Fehlerfall aus, ohne das Ergebnis zu verändern.
     * Nützlich für Debugging oder Logging.
     *
     * <p>Beispiel:
     * <pre>{@code
     * ValidationResult<String> result = validateInput("test");
     * result.peek(
     * s -> System.out.println("Erfolgreich validiert: " + s),
     * errors -> System.err.println("Fehler aufgetreten: " + errors)
     * );
     * // Der ursprüngliche Result-Typ bleibt erhalten.
     * }
     * </pre>
     * </p>
     *
     * @param onSuccess der Consumer, der bei Erfolg auf den Wert angewendet wird.
     * @param onFailure der Consumer, der bei Fehler auf die Fehlermeldungen angewendet wird.
     * @return die ursprüngliche {@link ValidationResult}-Instanz.
     */
    default ValidationResult<T> peek(Consumer<T> onSuccess, Consumer<List<String>> onFailure) {
        return switch (this) {
            case Success<T>(var value) -> {
                onSuccess.accept(value);
                yield this;
            }
            case Failure<T>(var errors) -> {
                onFailure.accept(errors);
                yield this;
            }
        };
    }

    /**
     * Gibt die Liste der Fehlermeldungen zurück, wenn es sich um ein fehlgeschlagenes Ergebnis handelt.
     * Andernfalls gibt es eine leere Liste zurück.
     *
     * @return Eine Liste von Strings, die die Fehlermeldungen enthält.
     */
    default List<String> getErrors() {
        return fold(value -> new ArrayList<>(), errorsList -> errorsList);
    }
}