package com.example.sandbox.validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


// @formatter:off
/**
 * Eine Sammlung statischer Hilfsmethoden zur Erstellung gebräuchlicher {@link Validator}-Instanzen.
 * <p>
 * Diese Utility-Klasse dient der Wiederverwendung typischer Validierungslogik – wie z.B.
 * das Prüfen auf {@code null}, leere Strings oder Wertebereiche.
 * </p>
 *
 * <h3>Beispiel:</h3>
 * <pre>{@code
 * Validator<Person> personValidator = Validators.all(
 *     Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
 *     Validators.inRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
 *     Validators.nested(Person::getAddress,
 *         Validators.all(
 *             Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
 *             Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
 *         )
 *     )
 * );
 * }</pre>
 * <h3>Beispiel:</h3>
 * <pre>{@code
 * public class Booking {
 *     private List<String> participants;
 *     private Double price;
 *     private LocalDate date;
 *
 *     // Getter & Setter
 * }
 *
 * // Dann sieht die Validierung z.B. so aus:
 *
 * Validator<Booking> bookingValidator = Validators.all(
 *     Validators.notEmpty(Booking::getParticipants, "Teilnehmerliste darf nicht leer sein"),
 *     Validators.numberInRange(Booking::getPrice, 0.01, 9999.99, "Preis muss positiv sein"),
 *     Validators.dateInFuture(Booking::getDate, "Datum muss in der Zukunft liegen")
 * );
 *
 * // Oder im Builder:
 *
 * ValidatingBuilder<Booking> builder = new ValidatingBuilder<>(Booking::new)
 *     .with(Booking::setParticipants, List.of("Alice", "Bob"))
 *     .with(Booking::setPrice, 199.99)
 *     .with(Booking::setDate, LocalDate.now().plusDays(5))
 *     .withValidator(bookingValidator);
 *
 * ValidationResult<Booking> result = builder.buildResult();
 * }</pre>
 *
 * <h3>Beispiel:</h3>
 * <pre>{@code
 * ValidatingBuilder<Event> builder = new ValidatingBuilder<>(Event::new)
 *     .with(Event::setTitle, "ChatGPT-Workshop")
 *     .with(Event::setStartDate, LocalDate.now().plusDays(5))
 *     .with(Event::setParticipants, List.of("Anna", "Bob"))
 *     .with(Event::setPrice, 59.90)
 *     .withValidator(Validators.notBlank(Event::getTitle, "Titel darf nicht leer sein"))
 *     .withValidator(Validators.notEmpty(Event::getParticipants, "Teilnehmerliste fehlt"))
 *     .withValidator(Validators.numberInRange(Event::getPrice, 0.01, 500.00, "Preis ungültig"))
 *     .withValidator(Validators.dateInFuture(Event::getStartDate, "Datum muss in der Zukunft liegen"));
 *
 * ValidationResult<Event> result = builder.buildResult();
 *
 * if (result.isValid()) {
 *     System.out.println("Event erfolgreich erstellt: " + result.getResult());
 * } else {
 *     System.out.println("Validierungsfehler:");
 *     result.getErrors().forEach(System.out::println);
 * }
 * }</pre>
 *
 * <h3>Beispiel:</h3>
 * <pre>{@code
 * Container<Order, Attribute> container = new Container<>();
 * container.setObject(new Order());
 * container.setAttributes(List.of(
 *     new Attribute("A001"),
 *     new Attribute(null)  // Fehler: Code ist null
 * ));
 *
 * // Wir wollen prüfen, dass jedes Attribut ein code != null hat.
 *
 * Validator<Attribute> attrValidator = Validator.of(
 *     a -> a.getCode() != null,
 *     "Code darf nicht null sein"
 * );
 *
 * // Kombiniere: List<Attribute> Validator + Container-Extractor
 * Validator<Container<Order, Attribute>> containerValidator = Validators.nested(
 *     Container::getAttributes,
 *     Validators.validateAllElements(attrValidator)
 * );
 *
 * // Anwendung im Builder
 * ValidatingBuilder<Container<Order, Attribute>> builder = new ValidatingBuilder<>(Container::new)
 *     .with(Container::setObject, new Order())
 *     .with(Container::setAttributes, container.getAttributes())
 *     .withValidator(containerValidator);
 *
 * ValidationResult<Container<Order, Attribute>> result = builder.buildResult();
 *
 * if (!result.isValid()) {
 *     result.getErrors().forEach(System.out::println);
 * }
 * }</pre>
 *
 * <h3>Beispiel:</h3>
 * <pre>{@code
 * Address home = new Address();
 * Person person = new Person();
 * person.setHome(home);
 *
 * // Hier wird allCollecting genutzt
 *
 * Validator<Address> addressValidator = Validators.allCollecting(
 *     Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
 *     Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
 * );
 *
 * // Hier wird ebenfalls allCollecting genutzt
 *
 * Validator<Person> personValidator = Validators.allCollecting(
 *     Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
 *     Validators.numberInRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
 *     Validators.notEmpty(Person::getAddresses, "Die List der Adressen darf nicht leer sein"),
 *     Validators.nested(Person::getHome, addressValidator, "Es gibt keine Heimatadresse")
 * );
 *
 * ValidationResult<Person> result = personValidator.validate(person);
 *
 * if (!result.isValid()) {
 *     // Alle Fehler, die er gefunden hat, werden ausgegeben
 *
 *     System.out.println(result.getErrors());
 * }
 * }</pre>
 */
public final class Validators {

    private Validators() {
        // statische Hilfsklasse – kein Konstruktor erlaubt
    }

    /**
     * Erstellt einen Validator, der prüft, ob ein bestimmtes Feld nicht {@code null} ist.
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.notNull(Person::getName, "Name darf nicht leer sein");
     * }</pre>
     *
     * @param getter       Getter-Funktion auf das zu prüfende Feld
     * @param errorMessage Fehlermeldung, falls das Feld {@code null} ist
     * @param <T>          Der Typ des zu validierenden Objekts.
     * @param <U>          Typ des Feldes
     * @return ein {@link Validator}, der auf {@code null} prüft
     */
    public static <T, U> Validator<T> notNull(Function<T, U> getter, String errorMessage) {
        return Validator.of(obj -> getter.apply(obj) != null, errorMessage);
    }

    /**
     * Validiert, dass eine {@link Collection} nicht {@code null} und nicht leer ist.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Team> validator = Validators.notEmpty(Team::getMembers, "Team darf nicht leer sein");
     * }</pre>
     *
     * @param getter Funktion, die das Collection-Feld extrahiert
     * @param errorMessage Fehlermeldung, falls leer oder {@code null}
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param <U> Typ der Collection (z.B. List<String>)
     * @return ein {@link Validator}, der auf Nicht-Leere prüft
     */
    public static <T, U extends Collection<?>> Validator<T> notEmpty(Function<T, U> getter, String errorMessage) {
        return Validator.of(obj -> {
            U value = getter.apply(obj);
            return value != null && !value.isEmpty();
        }, errorMessage);
    }

    /**
     * Erstellt einen Validator, der prüft, ob ein String-Feld nicht leer oder nur aus Leerzeichen besteht.
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.notBlank(Person::getName, "Name darf nicht leer sein");
     * }</pre>
     *
     * @param getter       Getter-Funktion auf das zu prüfende Feld
     * @param errorMessage Fehlermeldung, falls der String leer ist
     * @param <T>          Der Typ des zu validierenden Objekts.
     * @return ein {@link Validator}, der auf Nicht-Leere prüft
     */
    public static <T> Validator<T> notBlank(Function<T, String> getter, String errorMessage) {
        return Validator.of(obj -> {
            String value = getter.apply(obj);
            return value != null && !value.isBlank();
        }, errorMessage);
    }

    /**
     * Validiert, dass ein vergleichbarer numerischer Wert innerhalb eines
     * bestimmten Bereichs liegt (inkl. {@code min} und {@code max}).
     * <p>
     * Diese Methode funktioniert für alle Typen, die {@link Comparable}
     * sind – z.B. {@link Integer}, {@link Double}, {@link java.math.BigDecimal}, etc.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * public class Order {
     *     private BigDecimal totalAmount;
     *
     *     public BigDecimal getTotalAmount() { return totalAmount; }
     *     public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
     * }
     *
     * Validator<Order> amountValidator = Validators.numberInRange(
     *     Order::getTotalAmount,
     *     BigDecimal.valueOf(10),
     *     BigDecimal.valueOf(5000),
     *     "Gesamtbetrag muss zwischen 10 und 5000 liegen"
     * );
     * }</pre>
     *
     * @param getter       Funktion, die den zu validierenden Wert extrahiert
     * @param min          Untere Grenze (inklusive)
     * @param max          Obere Grenze (inklusive)
     * @param errorMessage Fehlermeldung bei Ungültigkeit
     * @param <T>          Der Typ des zu validierenden Objekts.
     * @param <N>          Typ des Werts, der validiert werden soll (muss {@link Comparable} sein)
     * @return ein {@link Validator}, der prüft, ob der Wert im Bereich liegt
     */
    public static <T, N extends Comparable<N>> Validator<T> numberInRange(
        Function<T, N> getter, N min, N max, String errorMessage) {
        return valueInRange(getter, min, max, Boundary.CLOSED, Boundary.CLOSED, errorMessage);
    }

    /**
     * Erstellt einen Validator, der prüft, ob ein extrahierter Wert
     * kleiner oder gleich einem gegebenen Maximalwert ist.
     * <p>
     * Diese Methode verwendet intern {@link #valueBefore(Function, Comparable, Boundary, String)}
     * mit einer geschlossenen Obergrenze ({@link Boundary#CLOSED}).
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.max(
     *     Person::getAge,
     *     65,
     *     "Das Alter darf 65 nicht überschreiten"
     * );
     *
     * ValidationResult<Person> result = validator.validate(person);
     * if (!result.isValid()) {
     *     System.out.println(result.getErrors());
     * }
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param <N> Der Typ des Wertes, der validiert wird (muss {@link Comparable} sein).
     * @param getter        Funktion zur Extraktion des zu prüfenden Werts aus dem Objekt.
     * @param max           Der maximale Grenzwert, der akzeptiert wird.
     * @param errorMessage  Fehlermeldung im Falle einer ungültigen Validierung.
     * @return Ein Validator, der prüft, ob der extrahierte Wert kleiner oder gleich {@code max} ist.
     */
    public static <T, N extends Comparable<N>> Validator<T> max(
        Function<T, N> getter, N max, String errorMessage) {
        return valueBefore(getter, max, Boundary.CLOSED, errorMessage);
    }

    /**
     * Erstellt einen Validator, der prüft, ob ein extrahierter Wert vor
     * einem maximalen Grenzwert liegt.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.valueBefore(
     *     Person::getBirthDate,
     *     LocalDate.of(2020, 1, 1),
     *     Boundary.OPEN,
     *     "Geburtsdatum muss vor 2020 liegen"
     * );
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param <N> Der Typ des zu validierenden Wertes (muss {@link Comparable} sein).
     * @param getter        Funktion zur Extraktion des zu prüfenden Werts aus dem Objekt
     * @param max           Der maximale Grenzwert, mit dem verglichen wird
     * @param maxBoundary   Gibt an, ob die obere Grenze inklusive (CLOSED) oder exklusiv (OPEN) ist
     * @param errorMessage  Fehlermeldung im Falle einer ungültigen Validierung
     * @return Ein Validator, der prüft, ob der extrahierte Wert kleiner (oder kleiner gleich) dem Grenzwert ist
     */
    public static <T, N extends Comparable<N>> Validator<T> valueBefore(
        Function<T, N> getter, N max, Boundary maxBoundary, String errorMessage) {
        return Validator.of(obj -> {
            N value = getter.apply(obj);
            if (value == null || max == null) return false;

            return switch (maxBoundary) {
                case CLOSED -> value.compareTo(max) <= 0;
                case OPEN -> value.compareTo(max) < 0;
            };
        }, errorMessage);
    }

    /**
     * Erstellt einen Validator, der prüft, ob ein extrahierter Wert
     * größer oder gleich einem gegebenen Minimalwert ist.
     * <p>
     * Diese Methode verwendet intern {@link #valueAfter(Function, Comparable, Boundary, String)}
     * mit einer geschlossenen Untergrenze (Boundary.CLOSED).
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.min(
     *     Person::getAge,
     *     18,
     *     "Mindestalter ist 18 Jahre"
     * );
     *
     * ValidationResult<Person> result = validator.validate(person);
     * if (!result.isValid()) {
     *     System.out.println(result.getErrors());
     * }
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param <N> Der Typ des Wertes, der validiert wird (muss {@link Comparable} sein).
     * @param getter        Funktion zur Extraktion des zu prüfenden Werts aus dem Objekt.
     * @param min           Der minimale Grenzwert, der akzeptiert wird.
     * @param errorMessage  Fehlermeldung im Falle einer ungültigen Validierung.
     * @return Ein Validator, der prüft, ob der extrahierte Wert größer oder gleich {@code min} ist.
     */
    public static <T, N extends Comparable<N>> Validator<T> min(
        Function<T, N> getter, N min, String errorMessage) {
        return valueAfter(getter, min, Boundary.CLOSED, errorMessage);
    }

    /**
     * Erstellt einen Validator, der prüft, ob ein extrahierter Wert nach einem minimalen Grenzwert liegt.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.valueAfter(
     *     Person::getBirthDate,
     *     LocalDate.of(1900, 1, 1),
     *     Boundary.CLOSED,
     *     "Geburtsdatum darf nicht vor 1900 liegen"
     * );
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param <N> Der Typ des zu validierenden Wertes (muss {@link Comparable} sein).
     * @param getter        Funktion zur Extraktion des zu prüfenden Werts aus dem Objekt
     * @param min           Der minimale Grenzwert, mit dem verglichen wird
     * @param minBoundary   Gibt an, ob die untere Grenze inklusive (CLOSED) oder exklusiv (OPEN) ist
     * @param errorMessage  Fehlermeldung im Falle einer ungültigen Validierung
     * @return Ein Validator, der prüft, ob der extrahierte Wert größer (oder größer gleich) dem Grenzwert ist
     */
    public static <T, N extends Comparable<N>> Validator<T> valueAfter(
        Function<T, N> getter, N min, Boundary minBoundary, String errorMessage) {
        return Validator.of(obj -> {
            N value = getter.apply(obj);
            if (value == null || min == null) return false;

            return switch (minBoundary) {
                case CLOSED -> value.compareTo(min) >= 0;
                case OPEN -> value.compareTo(min) > 0;
            };
        }, errorMessage);
    }

    /**
     * Kombiniert zwei Validatoren logisch mit UND.
     * Der zweite Validator wird nur ausgeführt, wenn der erste erfolgreich ist.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.and(
     *     Validators.notBlank(Person::getName, "Name fehlt"),
     *     Validators.numberInRange(Person::getAge, 18, 99, "Alter ungültig")
     * );
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param first  Der erste Validator
     * @param second Der zweite Validator
     * @return Ein kombinierter Validator, der nur dann gültig ist, wenn beide gültig sind
     */
    public static <T> Validator<T> and(Validator<T> first, Validator<T> second) {
        return t -> first.and(second).validate(t);
    }

    /**
     * Kombiniert zwei Validatoren logisch mit ODER.
     * Das Ergebnis ist gültig, wenn mindestens einer der Validatoren gültig ist.
     *
     * <h3>Beispiel</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.or(
     *     Validators.nested(Person::getHome,
     *         Validators.condition(home -> home.getCity().equals("London"), "Ihr Heimatort ist nicht London"),
     *         "Kein Heimatort gefunden"),
     *     Validators.numberInRange(Person::getAge, 18, 99, "Sie sind nicht über 18 Jahre alt.")
     * );
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param first  Der erste Validator
     * @param second Der zweite Validator
     * @return Ein kombinierter Validator, der nur dann gültig ist, wenn mindestens
     * einer der gültig ist
     */
    public static <T> Validator<T> or(Validator<T> first, Validator<T> second) {
        return t -> first.or(second).validate(t);
    }

    /**
     * Erzeugt einen Validator, der prüft, ob ein extrahierter numerischer Wert in einem gegebenen Intervall liegt.
     * Dabei kann die Inklusivität oder Exklusivität der unteren und oberen Grenze gesteuert werden.
     *
     * <p>Diese Methode eignet sich für alle Typen, die {@link Comparable} implementieren,
     * wie z.B. {@code Integer}, {@code Long}, {@code Double}, {@code BigDecimal}, {@code LocalDateTime} usw.</p>
     *
     * <h3>Beispiele</h3>
     * <pre>{@code
     * // Beispiel 1: Alter muss zwischen 18 und 99 (beide inklusive) liegen: [18, 99]
     * Validator<Person> validator1 = Validators.valueInRange(
     *     Person::getAge, 18, 99,
     *     Boundary.INCLUSIVE, Boundary.INCLUSIVE,
     *     "Alter muss zwischen 18 und 99 liegen"
     * );
     *
     * // Beispiel 2: Wert muss größer als 0 und kleiner oder gleich 100 sein: (0, 100]
     * Validator<Zahl> validator2 = Validators.valueInRange(
     *     Zahl::getWert, 0, 100,
     *     Boundary.EXCLUSIVE, Boundary.INCLUSIVE,
     *     "Wert muss > 0 und <= 100 sein"
     * );
     *
     * // Beispiel 3: Datum muss strikt zwischen zwei Zeitpunkten liegen: (start, end)
     * Validator<Event> validator3 = Validators.valueInRange(
     *     Event::getZeitpunkt,
     *     LocalDateTime.of(2024, 1, 1, 0, 0),
     *     LocalDateTime.of(2024, 12, 31, 23, 59),
     *     Boundary.EXCLUSIVE, Boundary.EXCLUSIVE,
     *     "Zeitpunkt muss im Jahr 2024 liegen (exklusive)"
     * );
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts.
     * @param <N> Der Typ des zu validierenden Wertes (muss {@link Comparable} sein).
     * @param getter Eine Funktion, die aus dem Objekt den zu prüfenden Wert extrahiert.
     * @param min Der untere Grenzwert des Intervalls.
     * @param max Der obere Grenzwert des Intervalls.
     * @param minBoundary Gibt an, ob die untere Grenze inklusive oder exklusiv sein soll.
     * @param maxBoundary Gibt an, ob die obere Grenze inklusive oder exklusiv sein soll.
     * @param message Die Fehlermeldung, die im Fehlerfall zurückgegeben wird.
     * @return Ein {@link Validator}, der das Objekt validiert.
     */
    public static <T, N extends Comparable<N>> Validator<T> valueInRange(
        Function<T, N> getter, N min, N max, Boundary minBoundary, Boundary maxBoundary, String message) {
        return and(
            valueAfter(getter, min, minBoundary, message),
            valueBefore(getter, max, maxBoundary, message)
        );
    }

    /**
     * Erstellt einen Validator, der prüft, ob der aus dem Objekt extrahierte Wert gleich dem erwarteten Wert ist.
     *
     * <p>Der Vergleich wird mit {@link java.util.Objects#equals(Object, Object)} durchgeführt,
     * sodass auch <code>null</code>-Werte sicher verglichen werden können.</p>
     *
     * <h3>Beispiel</h3>
     * <pre>{@code
     * Validator<User> validator = Validators.equalTo(User::getStatus, Status.ACTIVE, "Benutzer ist nicht aktiv");
     *
     * User user = new User();
     * user.setStatus(Status.INACTIVE);
     *
     * ValidationResult<User> result = validator.validate(user);
     * if (!result.isValid()) {
     *     System.out.println(result.getErrors()); // ["Benutzer ist nicht aktiv"]
     * }
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts
     * @param <V> Der Typ des zu vergleichenden Werts
     * @param getter Eine Funktion, die aus dem Objekt den zu vergleichenden Wert extrahiert
     * @param expected Der erwartete Soll-Wert, mit dem verglichen werden soll
     * @param message Die Fehlermeldung, falls die Werte nicht übereinstimmen
     * @return Ein Validator, der erfolgreich ist, wenn die Werte gleich sind
     */
    public static <T, V> Validator<T> equalTo(Function<T, V> getter, V expected, String message) {
        return Validator.of(obj -> Objects.equals(getter.apply(obj), expected), message);
    }

    /**
     * Erstellt einen Validator, der prüft, ob der aus dem Objekt extrahierte Wert gemäß dem übergebenen Comparator
     * gleich dem erwarteten Wert ist.
     *
     * <h3>Beispiel</h3>
     * <pre>{@code
     * Validator<User> validator = Validators.equalTo(
     *     User::getName,
     *     "max",
     *     String.CASE_INSENSITIVE_ORDER,
     *     "Name stimmt nicht überein"
     * );
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts
     * @param <V> Der Typ des zu vergleichenden Werts
     * @param getter Funktion zur Extraktion des Werts aus dem Objekt
     * @param expected Der erwartete Wert
     * @param comparator Comparator zur Definition der Gleichheit
     * @param message Fehlermeldung bei Ungleichheit
     * @return Ein Validator, der erfolgreich ist, wenn der Vergleich mit 0 endet (d.h. gleich laut Comparator)
     */
    public static <T, V> Validator<T> equalTo(
        Function<T, V> getter,
        V expected,
        Comparator<V> comparator,
        String message
    ) {
        return Validator.of(obj -> {
            V actual = getter.apply(obj);
            return actual != null && expected != null && comparator.compare(actual, expected) == 0;
        }, message);
    }


    /**
     * Erstellt einen Validator, der prüft, ob zwei Felder eines Objekts gleich sind.
     *
     * <p>Die beiden Werte werden mit {@link java.util.Objects#equals(Object, Object)} verglichen,
     * um auch <code>null</code>-sicher zu sein.</p>
     *
     * <h3>Beispiel</h3>
     * <pre>{@code
     * Validator<User> validator = Validators.fieldsEqual(
     *     User::getPassword,
     *     User::getPasswordRepeat,
     *     "Passwörter stimmen nicht überein"
     * );
     *
     * User user = new User();
     * user.setPassword("geheim");
     * user.setPasswordRepeat("anders");
     *
     * ValidationResult<User> result = validator.validate(user);
     * if (!result.isValid()) {
     *     System.out.println(result.getErrors()); // ["Passwörter stimmen nicht überein"]
     * }
     * }</pre>
     *
     * @param <T> Der Typ des zu validierenden Objekts
     * @param <U> Der Typ des zu vergleichenden Objekts
     * @param <V> Der Typ des zu vergleichenden Objekts
     * @param getter1 Funktion zur Extraktion des ersten Werts aus dem Objekt
     * @param getter2 Funktion zur Extraktion des zweiten Werts aus dem Objekt
     * @param message Die Fehlermeldung, falls die Werte nicht gleich sind
     * @return Ein Validator, der erfolgreich ist, wenn beide Werte gleich sind
     */
    public static <T, U, V> Validator<T> fieldsEqual(Function<T, U> getter1, Function<T, V> getter2, String message) {
        return Validator.of(obj -> Objects.equals(getter1.apply(obj), getter2.apply(obj)), message);
    }

    /**
     * Validiert, dass ein Datum in der Zukunft liegt (nach dem heutigen Datum).
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Event> validator = Validators.dateInFuture(Event::getStartDate, "Datum muss in der Zukunft liegen");
     * }</pre>
     *
     * @param getter Funktion zur Extraktion des {@link LocalDate}
     * @param errorMessage Fehlermeldung bei ungültigem Datum
     * @param <T> Der Typ des zu validierenden Objekts.
     * @return ein {@link Validator} für zukünftige Daten
     */
    public static <T> Validator<T> dateInFuture(Function<T, LocalDate> getter, String errorMessage) {
        return Validator.of(obj -> {
            LocalDate date = getter.apply(obj);
            return date != null && date.isAfter(LocalDate.now());
        }, errorMessage);
    }

    /**
     * Validiert, dass ein Datum in der Vergangenheit liegt (vor dem heutigen Datum).
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.dateInPast(Person::getBirthdate, "Geburtsdatum muss in der Vergangenheit liegen");
     * }</pre>
     *
     * @param getter Funktion zur Extraktion des {@link LocalDate}
     * @param errorMessage Fehlermeldung bei ungültigem Datum
     * @param <T> Der Typ des zu validierenden Objekts.
     * @return ein {@link Validator} für vergangene Daten
     */
    public static <T> Validator<T> dateInPast(Function<T, LocalDate> getter, String errorMessage) {
        return Validator.of(obj -> {
            LocalDate date = getter.apply(obj);
            return date != null && date.isBefore(LocalDate.now());
        }, errorMessage);
    }

    /**
     * Validiert, dass ein Datum innerhalb eines bestimmten Intervalls liegt (inklusive min und max).
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Project> validator = Validators.dateInRange(
     *     Project::getDeadline,
     *     LocalDate.now(),
     *     LocalDate.now().plusMonths(6),
     *     "Deadline muss innerhalb der nächsten 6 Monate liegen"
     * );
     * }</pre>
     *
     * @param getter Funktion zur Extraktion des {@link LocalDate}
     * @param min Untere Schranke
     * @param max Obere Schranke
     * @param errorMessage Fehlermeldung bei Ungültigkeit
     * @param <T> Der Typ des zu validierenden Objekts.
     * @return ein {@link Validator}, der das Datum prüft
     */
    public static <T> Validator<T> dateInRange(Function<T, LocalDate> getter, LocalDate min, LocalDate max, String errorMessage) {
        return Validator.of(obj -> {
            LocalDate date = getter.apply(obj);
            return date != null &&
                (date.isEqual(min) || date.isAfter(min)) &&
                (date.isEqual(max) || date.isBefore(max));
        }, errorMessage);
    }

    /**
     * Validiert, ob alle Schlüssel und Werte in einem Map-Objekt nicht null, nicht leer und nicht nur aus Leerzeichen bestehen.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * // Beispiel für die Verwendung:
     * Validator<Map<String, String>> validator = mapNotEmptyKeysAndValues("Die Attributliste ist leer oder fehlt");
     *
     * Map<String, String> attributes = Map.of(
     *     "key1", "value1",
     *     " ",    "value2",
     *     "key3", ""
     * );
     *
     * ValidationResult result = validator.validate(attributes);
     *
     * if (result.isSuccess()) {
     *     System.out.println("Validation erfolgreich: " + result.getData());
     * } else {
     *     System.out.println("Validationsfehler:");
     *     for (String error : result.getErrors()) {
     *         System.out.println(error);
     *     }
     * }
     * // Ausgabe:
     * // Validationsfehler:
     * // Ungültiges Attribut: Key=' ', Value='value2'
     * // Ungültiges Attribut: Key='key3', Value=''
     * }</pre>
     *
     * @param errorMessage Die Fehlermeldung, die verwendet wird, wenn die Karte selbst null oder leer ist.
     * @return Ein Validator-Objekt, das eine Map von Strings auf Strings validiert.
     */
    public static Validator<Map<String, String>> mapNotEmptyKeysAndValues(String errorMessage) {
        return map -> {
            if (map == null || map.isEmpty()) {
                return ValidationResult.failure(List.of(errorMessage));
            }

            List<String> errors = map.entrySet().stream()
                .filter(e -> e.getKey() == null || e.getKey().isBlank() || e.getValue() == null || e.getValue().isBlank())
                .map(e -> "Ungültiges Attribut: Key='" + e.getKey() + "', Value='" + e.getValue() + "'")
                .toList();

            return errors.isEmpty() ? ValidationResult.success(map) : ValidationResult.failure(errors);
        };
    }

    /**
     * Erstellt einen benutzerdefinierten Validator basierend auf einem Prädikat.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.condition( p -> p.getAge() > 20,
     *             "Sie sind nicht älter als 20");
     * }</pre>
     *
     * @param predicate    Bedingung zur Prüfung
     * @param errorMessage Fehlermeldung bei Nichterfüllung
     * @param <T>          Der Typ des zu validierenden Objekts.
     * @return ein {@link Validator}
     */
    public static <T> Validator<T> condition(Predicate<T> predicate, String errorMessage) {
        return Validator.of(predicate, errorMessage);
    }

    /**
     * Erzeugt einen Validator, der überprüft, ob ein String-Feld eines Objekts einem bestimmten regulären Ausdruck entspricht.
     * <p>
     * Diese Methode eignet sich zum Beispiel zur Validierung von E-Mail-Adressen, Postleitzahlen oder benutzerdefinierten Formaten.
     *
     * <h4>Beispiel:</h4>
     * <pre>{@code
     * public class Benutzer {
     *     private String email;
     *
     *     public String getEmail() {
     *         return email;
     *     }
     * }
     *
     * Validator<Benutzer> emailValidator = Validators.matchesPattern(
     *     Benutzer::getEmail,
     *     "^[\\w.-]+@[\\w.-]+\\.\\w{2,}$",
     *     "Ungültige E-Mail-Adresse"
     * );
     *
     * ValidationResult<Benutzer> result = emailValidator.validate(new Benutzer("abc@domain.de"));
     * }</pre>
     *
     * @param <T>           Der Typ des zu validierenden Objekts.
     * @param getter        Eine Funktion, die aus dem Objekt den zu prüfenden String extrahiert.
     * @param regex         Der reguläre Ausdruck, dem der extrahierte String entsprechen muss.
     * @param errorMessage  Die Fehlermeldung, die zurückgegeben wird, falls die Validierung fehlschlägt.
     * @return              Ein Validator, der true zurückgibt, wenn der extrahierte String dem Muster entspricht.
     */
    public static <T> Validator<T> matchesPattern(Function<T, String> getter, String regex, String errorMessage) {
        return Validator.of(obj -> {
            String value = getter.apply(obj);
            return value != null && value.matches(regex);
        }, errorMessage);
    }

    /**
     * Erzeugt einen bedingten Validator: Wenn die angegebene Bedingung erfüllt ist,
     * wird der übergebene Validator ausgeführt. Andernfalls wird die Validierung übersprungen
     * und als erfolgreich gewertet.
     * <p>
     * Diese Methode ist hilfreich bei abhängigen Feldern: z.B. wenn ein Feld nur unter bestimmten
     * Bedingungen geprüft werden soll (z.B. „wenn Checkbox aktiv, dann muss Kommentar gesetzt sein“).
     *
     * <h4>Beispiel:</h4>
     * <pre>{@code
     * public class Feedback {
     *     private boolean kontaktErlaubt;
     *     private String email;
     *
     *     public boolean isKontaktErlaubt() { return kontaktErlaubt; }
     *     public String getEmail() { return email; }
     * }
     *
     * Validator<Feedback> kontaktValidator = Validators.ifThen(
     *     Feedback::isKontaktErlaubt,
     *     Validators.notBlank(Feedback::getEmail, "E-Mail ist erforderlich, wenn Kontakt erlaubt ist.")
     * );
     *
     * Feedback f = new Feedback(true, null);
     * ValidationResult<Feedback> result = kontaktValidator.validate(f);
     * }</pre>
     *
     * @param <T>           Der Typ des zu prüfenden Objekts.
     * @param condition     Eine Bedingung, bei deren Erfüllung der zweite Validator ausgeführt wird.
     * @param thenValidator Der Validator, der angewendet wird, wenn die Bedingung erfüllt ist.
     * @return              Ein zusammengesetzter Validator, der bedingt prüft.
     */
    public static <T> Validator<T> ifThen(Predicate<T> condition, Validator<T> thenValidator) {
        return t -> {
            if (condition.test(t)) {
                return thenValidator.validate(t);
            }
            return ValidationResult.success(t);
        };
    }


    /**
     * Kombiniert mehrere Validatoren in einen einzigen, der sie alle in Reihenfolge prüft.
     * <p>
     * Beim ersten Fehler wird die Validierung abgebrochen.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> personValidator = Validators.all(
     *     Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
     *     Validators.inRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
     *     Validators.nested(Person::getAddress,
     *         Validators.all(
     *             Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
     *             Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
     *         )
     *     )
     * );
     * }</pre>
     *
     * @param validators Validatoren, die ausgeführt werden sollen
     * @param <T>        Der Typ des zu validierenden Objekts.
     * @return ein kombinierter Validator
     */
    @SafeVarargs
    public static <T> Validator<T> all(Validator<T>... validators) {
        return t -> {
            ValidationResult<T> result = ValidationResult.success(t);
            for (Validator<T> validator : validators) {
                result = result.flatMap(validator::validate);
                if (!result.isValid()) break;
            }
            return result;
        };
    }

    /**
     * Gibt einen Validator zurück, der mehrere Validatoren ausführt und dabei alle Fehler sammelt.
     * <p>
     * Anders als {@link #all( Validator[])} bricht dieser Validator nicht beim ersten Fehler ab,
     * sondern führt alle Validatoren aus und sammelt sämtliche Fehlermeldungen.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * public class User {
     *     private String name;
     *     private String email;
     *     private Integer age;
     *
     *     // Getter...
     * }
     *
     * Validator<User> validator = Validators.allCollecting(List.of(
     *     Validators.notNull(User::getName, "Name darf nicht leer sein"),
     *     Validators.notNull(User::getEmail, "E-Mail darf nicht leer sein"),
     *     Validators.inRange(User::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen")
     * ));
     *
     * ValidationResult<User> result = validator.validate(new User());
     *
     * if (!result.isValid()) {
     *     System.out.println(result.errors()); // → gibt alle Fehler zurück
     * }
     * }</pre>
     *
     * @param validators Liste von Validatoren, die alle angewendet werden
     * @param <T>        Typ des zu validierenden Objekts
     * @return ein Validator, der alle Fehler aggregiert
     */
    public static <T> Validator<T> allCollecting(List<Validator<T>> validators) {
        return t -> {
            List<String> errors = validators.stream()
                .map(v -> v.validate(t))
                .filter(result -> !result.isValid())
                .flatMap(result -> result.getErrors().stream())
                .toList();

            return errors.isEmpty()
                ? ValidationResult.success(t)
                : ValidationResult.failure(errors);
        };
    }

    /**
     * Führt alle übergebenen Validatoren aus und sammelt dabei alle Fehler (bricht nicht nach dem ersten Fehler ab).
     * <p>
     * Diese Varargs-Variante ist eine bequeme Alternative zu {@link #allCollecting(List)}.
     *
     * @param validators Die Validatoren, die alle ausgeführt werden sollen
     * @param <T>        Der Typ des zu validierenden Objekts
     * @return Ein kombinierter Validator, der alle Fehler einsammelt
     * @see #allCollecting(List)
     */
    @SafeVarargs
    public static <T> Validator<T> allCollecting(Validator<T>... validators) {
        return allCollecting(Arrays.asList(validators));
    }


    /**
     * Erzeugt einen Validator für ein verschachteltes Objekt.
     * <p>
     * Der extrahierte Teil wird mit dem übergebenen Validator geprüft.
     * Ist dieser gültig, wird das ursprüngliche Objekt als gültig betrachtet.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     *   Validator<Person> addressValidator = Validators.nested(
     *       Person::getAddress,
     *       Validator.of(Address::isValid, "Adresse ungültig")
     *   );
     * }</pre>
     *
     * @param extractor Funktion, die das zu prüfende Teilobjekt extrahiert
     * @param validator Validator für das Teilobjekt
     * @param <T> Typ des Ursprungsobjekts
     * @param <U> Typ des Teilobjekts
     * @return Ein Validator für das Ursprungsobjekt
     */
    public static <T, U> Validator<T> nested(Function<T, U> extractor, Validator<U> validator, String nullMessage) {
        return t -> {
            U value = extractor.apply(t);
            if (value == null) {
                return ValidationResult.failure(List.of(nullMessage));
            }
            return validator.validate(value).map(x -> t);
        };
    }

    /**
     * Bequemer Zugriff auf {@link Validator#onIfPresent(Function)}.
     * Wird nur aufgerufen, wenn der extrahierte Wert nicht {@code null} ist.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<Person> validator = Validators.onIfPresent(
     *     Person::getHome,
     *     Validators.notBlank(Address::getCity, "Stadt fehlt")
     * );
     * }</pre>
     *
     * @param <T> Typ des äußeren Objekts
     * @param <U> Typ des extrahierten inneren Objekts
     * @param extractor Extrahiert das zu prüfende Teilobjekt.
     * @param validator Der Validator, der auf das Teilobjekt angewendet wird.
     * @return Ein zusammengesetzter Validator für das äußere Objekt.
     * @see Validator#onIfPresent(Function)
     *
     * @see Validators#nested(Function, Validator, String) für eine Variante mit Fehlermeldung bei {@code null}
     */
    public static <T, U> Validator<T> onIfPresent(Function<T, U> extractor, Validator<U> validator) {
        return validator.onIfPresent(extractor);
    }


    /**
     * Validiert alle Elemente einer gegebenen Liste mit dem übergebenen Element-Validator.
     * Dabei werden alle Validierungsfehler aller Elemente gesammelt und zu einem kombinierten {@link ValidationResult} zusammengefasst.
     * <p>
     * Ist die Liste leer oder sind alle Elemente gültig, wird ein erfolgreiches ValidationResult zurückgegeben.
     * Andernfalls werden alle Fehlermeldungen der ungültigen Elemente aggregiert und im Fehler-Ergebnis zurückgegeben.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<String> nichtLeerValidator = Validators.notBlank(s -> s, "String darf nicht leer sein");
     * Validator<List<String>> listenValidator = Validators.validateAllElements(nichtLeerValidator);
     *
     * List<String> eingabe = List.of("Hallo", "", "Welt", "");
     * ValidationResult<List<String>> ergebnis = listenValidator.validate(eingabe);
     *
     * if (!ergebnis.isValid()) {
     *     System.out.println(ergebnis.getErrors());
     *     // Ausgabe: [Fehler in Element 1: String darf nicht leer sein, Fehler in Element 3: String darf nicht leer sein]
     * }
     * }</pre>
     *
     * @param elementValidator der Validator, der auf jedes Element der Liste angewendet wird
     * @param <A> der Typ der Elemente in der Liste
     * @return ein Validator, der jedes Element einer Liste validiert und Fehler sammelt
     */
    public static <A> Validator<List<A>> validateAllElements(Validator<A> elementValidator) {
        return list -> {
            if (list == null) {
                return ValidationResult.failure(Collections.singletonList("Die Liste ist null"));
            }

            List<String> errors = IntStream.range(0, list.size())
                .mapToObj(i -> {
                    ValidationResult<A> result = elementValidator.validate(list.get(i));
                    return result.isValid()
                        ? Collections.<String>emptyList()
                        : result.getErrors().stream()
                        .map(msg -> "Fehler in Element " + i + ": " + msg)
                        .collect(Collectors.toList());
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

            return errors.isEmpty()
                ? ValidationResult.success(list)
                : ValidationResult.failure(errors);
        };
    }

    /**
     * Validiert alle Elemente einer angegebenen Liste unter Verwendung des bereitgestellten Element-Validators.
     * Sammelt alle Validierungsfehler der einzelnen Elemente und gibt ein kombiniertes {@link ValidationResult} zurück.
     *
     * <p>
     * Falls die Liste leer ist oder alle Elemente gültig sind, wird ein erfolgreiches ValidationResult zurückgegeben.
     * Andernfalls werden alle Fehlermeldungen der ungültigen Elemente im Fehlerergebnis zusammengefasst.
     *
     * <h3>Beispiel:</h3>
     * <pre>{@code
     * Validator<String> notBlankValidator = Validators.notBlank(s -> s, "String darf nicht leer sein");
     * Validator<List<String>> listValidator = Validators.validateAllElements(notBlankValidator);
     *
     * List<String> input = List.of("Hallo", "", "Welt", "");
     * ValidationResult<List<String>> result = listValidator.validate(input);
     *
     * if (!result.isValid()) {
     *     System.out.println(result.getErrors());
     *     // Ausgabe: [String darf nicht leer sein, String darf nicht leer sein]
     * }
     * }</pre>
     *
     * @param elementValidator Der Validator, der auf jedes Element der Liste angewendet wird.
     * @param <A> Der Typ der Elemente in der Liste.
     * @return Ein Validator, der alle Elemente einer Liste validiert und dabei alle Fehler aggregiert.
     */
    public static <A> Validator<List<A>> validateAllElementsNoIndex(Validator<A> elementValidator) {
        return list -> {
            List<String> errors = list.stream()
                .map(elementValidator::validate)
                .filter(r -> !r.isValid())
                .flatMap(r -> r.getErrors().stream())
                .collect(Collectors.toList());

            return errors.isEmpty()
                ? ValidationResult.success(list)
                : ValidationResult.failure(errors);
        };
    }
}
// @formatter:on
