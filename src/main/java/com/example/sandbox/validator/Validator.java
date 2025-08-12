package com.example.sandbox.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>Eine funktionale Schnittstelle für Validierungsregeln.</p>
 *
 * <p>Ein {@code Validator} nimmt ein Objekt des Typs {@code T} entgegen und
 * gibt ein {@link ValidationResult} zurück, das entweder den validierten Wert
 * oder eine Liste von Fehlermeldungen enthält.
 * Das Interface ist so konzipiert, dass Validatoren einfach miteinander
 * kombiniert und verkettet werden können, um komplexe Validierungslogiken
 * aufzubauen.</p>
 *
 * @param <T> der Typ des zu validierenden Objekts.
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Führt die Validierung auf dem gegebenen Objekt aus.
     *
     * @param t das zu validierende Objekt.
     * @return ein {@link ValidationResult}, das das Ergebnis der Validierung enthält.
     */
    ValidationResult<T> validate(T t);

    // ---

    /**
     * Erzeugt einen einfachen Validator aus einem {@link Predicate} und einer Fehlermeldung.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Validator<String> notEmpty = Validator.of(s -> !s.isEmpty(), "Feld darf nicht leer sein.");
     * ValidationResult<String> result = notEmpty.validate("");
     * // result ist ein Failure mit der Nachricht "Feld darf nicht leer sein."
     * }</pre>
     * </p>
     *
     * @param predicate das Prädikat, das die Validierungsregel darstellt.
     * @param errorMessage die Fehlermeldung, die bei einem Misserfolg zurückgegeben wird.
     * @param <T> der Typ des zu validierenden Objekts.
     * @return ein neuer {@code Validator}.
     */
    static <T> Validator<T> of(Predicate<T> predicate, String errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");

        return t -> predicate.test(t)
                ? ValidationResult.success(t)
                : ValidationResult.failure(errorMessage);
    }

    /**
     * Erzeugt einen Validator direkt aus einer Funktion, die ein {@link ValidationResult} zurückgibt.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Function<String, ValidationResult<String>> checkLength = s ->
     * s.length() > 5 ? ValidationResult.success(s) : ValidationResult.failure("Mindestens 6 Zeichen.");
     *
     * Validator<String> lengthValidator = Validator.from(checkLength);
     * }</pre>
     * </p>
     *
     * @param func die Funktion, die die Validierungslogik enthält.
     * @param <T> der Typ des zu validierenden Objekts.
     * @return ein neuer {@code Validator}.
     */
    static <T> Validator<T> from(Function<T, ValidationResult<T>> func) {
        return func::apply;
    }

    // ---

    /**
     * Verknüpft diesen Validator mit einem anderen in einer logischen "UND"-Operation (kurzschließend).
     * Wenn die erste Validierung fehlschlägt, wird die zweite nicht ausgeführt.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Validator<String> notNull = Validator.of(Objects::nonNull, "Wert darf nicht null sein.");
     * Validator<String> notEmpty = Validator.of(s -> !s.isEmpty(), "Feld darf nicht leer sein.");
     *
     * Validator<String> combined = notNull.and(notEmpty);
     * ValidationResult<String> result = combined.validate(null); // Prüft auf null, bricht ab
     * // result ist ein Failure mit der Nachricht "Wert darf nicht null sein."
     * }</pre>
     * </p>
     *
     * @param other der andere Validator.
     * @return ein neuer Validator, der beide Regeln anwendet.
     */
    default Validator<T> and(Validator<T> other) {
        return t -> {
            ValidationResult<T> r1 = this.validate(t);
            if (!r1.isValid()) return r1;
            return other.validate(t);
        };
    }

    /**
     * Verknüpft diesen Validator mit einem anderen in einer logischen "ODER"-Operation.
     * Wenn die erste Validierung erfolgreich ist, wird die zweite nicht ausgeführt.
     * Wenn beide fehlschlagen, werden die Fehlermeldungen kombiniert.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Validator<String> isA = Validator.of("A"::equals, "Wert ist nicht 'A'");
     * Validator<String> isB = Validator.of("B"::equals, "Wert ist nicht 'B'");
     *
     * Validator<String> isAorB = isA.or(isB);
     * ValidationResult<String> result = isAorB.validate("C");
     * // result ist ein Failure mit den Nachrichten ["Wert ist nicht 'A'", "Wert ist nicht 'B'"]
     * }</pre>
     * </p>
     *
     * @param other der andere Validator.
     * @return ein neuer Validator, der die ODER-Regel anwendet.
     */
    default Validator<T> or(Validator<T> other) {
        return t -> {
            ValidationResult<T> r1 = this.validate(t);
            if (r1.isValid()) return r1;
            ValidationResult<T> r2 = other.validate(t);
            return r2.isValid() ? r2 : ValidationResult.failure(mergeErrors(r1, r2));
        };
    }

    /**
     * Kehrt das Ergebnis dieses Validators um.
     *
     * <p>Beispiel:
     * <pre>{@code
     * Validator<String> isBlank = s -> s.isBlank() ? ValidationResult.success(s) : ValidationResult.failure("Nicht blank");
     * Validator<String> notBlank = isBlank.not("Darf nicht blank sein.");
     *
     * ValidationResult<String> result = notBlank.validate("  ");
     * // result ist ein Failure mit der Nachricht "Darf nicht blank sein."
     * }</pre>
     * </p>
     *
     * @param errorMessage die Fehlermeldung für den umgekehrten Fall.
     * @return ein neuer Validator mit umgekehrter Logik.
     */
    default Validator<T> not(String errorMessage) {
        return t -> {
            ValidationResult<T> r = this.validate(t);
            return r.isValid()
                    ? ValidationResult.failure(errorMessage)
                    : ValidationResult.success(t);
        };
    }

    /**
     * Wendet diesen Validator auf einen Teil eines anderen Objekts an.
     *
     * <p>Beispiel:
     * <pre>{@code
     * // Angenommen, es gibt eine Klasse 'User' mit einer Methode 'getEmail()'
     * Validator<String> emailValidator = Validator.of(s -> s.contains("@"), "E-Mail muss ein '@' enthalten.");
     * Validator<User> userEmailValidator = emailValidator.on(User::getEmail);
     *
     * User user = new User("testuser"); // E-Mail ist "testuser"
     * ValidationResult<User> result = userEmailValidator.validate(user);
     * // result ist ein Failure mit der Nachricht "E-Mail muss ein '@' enthalten."
     * }</pre>
     * </p>
     *
     * @param extractor eine Funktion, die den zu validierenden Teil extrahiert.
     * @param <U> der Typ des übergeordneten Objekts.
     * @return ein neuer Validator für das übergeordnete Objekt.
     */
    default <U> Validator<U> on(Function<U, T> extractor) {
        return u -> {
            T extracted = extractor.apply(u);
            return this.validate(extracted).map(x -> u);
        };
    }

    /**
     * Wendet diesen Validator auf einen optionalen Teil eines anderen Objekts an.
     * Wenn der extrahierte Wert {@code null} ist, wird die Validierung als erfolgreich betrachtet.
     *
     * <p>Beispiel:
     * <pre>{@code
     * // Angenommen, die E-Mail eines Benutzers kann null sein
     * Validator<User> optionalEmailValidator = Validator
     * .of(s -> s.contains("@"), "E-Mail muss ein '@' enthalten.")
     * .onIfPresent(User::getEmail);
     *
     * User userWithNullEmail = new User(null);
     * ValidationResult<User> result = optionalEmailValidator.validate(userWithNullEmail);
     * // result ist ein Success, da die E-Mail-Validierung übersprungen wurde.
     * }</pre>
     * </p>
     *
     * @param extractor eine Funktion, die den optionalen Teil extrahiert.
     * @param <U> der Typ des übergeordneten Objekts.
     * @return ein neuer Validator für das übergeordnete Objekt.
     */
    default <U> Validator<U> onIfPresent(Function<U, T> extractor) {
        return u -> {
            T value = extractor.apply(u);
            return value != null ? this.validate(value).map(x -> u) : ValidationResult.success(u);
        };
    }

    // ---

    /**
     * Eine private Hilfsmethode, um Fehlermeldungen aus zwei {@link ValidationResult}-Objekten zu kombinieren.
     *
     * @param r1 das erste Validierungsergebnis.
     * @param r2 das zweite Validierungsergebnis.
     * @param <T> der Typ der Validierungsergebnisse.
     * @return eine kombinierte Liste von Fehlermeldungen.
     */
    private static <T> List<String> mergeErrors(ValidationResult<T> r1, ValidationResult<T> r2) {
        List<String> errors = new ArrayList<>();

        // Nutzt die fold-Methode um auf die Fehler zuzugreifen
        r1.fold(successValue -> null, errors::addAll);
        r2.fold(successValue -> null, errors::addAll);

        return errors;
    }
}