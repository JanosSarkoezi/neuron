package com.example.sandbox.validator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidatorsTest {

    // --- Einfache Validatoren ---

    @Test
    public void notNull_shouldReturnSuccess_whenValueIsNotNull() {
        Validator<String> validator = Validators.notNull(s -> s, "Darf nicht null sein");
        ValidationResult<String> result = validator.validate("Hallo");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void notNull_shouldReturnFailure_whenValueIsNull() {
        Validator<String> validator = Validators.notNull(s -> s, "Darf nicht null sein");
        ValidationResult<String> result = validator.validate(null);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("Darf nicht null sein");
    }

    @Test
    public void notBlank_shouldReturnSuccess_whenValueIsNotBlank() {
        Validator<String> validator = Validators.notBlank(s -> s, "Darf nicht leer sein");
        ValidationResult<String> result = validator.validate("test");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void notBlank_shouldReturnFailure_whenValueIsBlank() {
        Validator<String> validator = Validators.notBlank(s -> s, "Darf nicht leer sein");
        ValidationResult<String> result = validator.validate("   ");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("Darf nicht leer sein");
    }

//    @Test
//    public void valueInRange_shouldReturnSuccess_whenValueIsInInclusiveRange() {
//        Validator<Integer> validator = Validators.valueInRange(i -> i, 5, 10, "Muss zwischen 5 und 10 liegen");
//        ValidationResult<Integer> result = validator.validate(7);
//        assertThat(result.isValid()).isTrue();
//    }
//
//    @Test
//    public void valueInRange_shouldReturnFailure_whenValueIsOutsideRange() {
//        Validator<Integer> validator = Validators.valueInRange(i -> i, 5, 10, "Muss zwischen 5 und 10 liegen");
//        ValidationResult<Integer> result = validator.validate(12);
//        assertThat(result.isValid()).isFalse();
//        assertThat(result.getErrors()).containsExactly("Muss zwischen 5 und 10 liegen");
//    }

    @Test
    public void numberInRange_shouldReturnSuccess_whenValueIsInRange() {
        Validator<Integer> validator = Validators.numberInRange(i -> i, 10, 20, "Muss zwischen 10 und 20 liegen");
        ValidationResult<Integer> result = validator.validate(15);
        assertThat(result.isValid()).isTrue();
    }

//    @Test
//    public void matchesRegex_shouldReturnSuccess_whenValueMatches() {
//        Validator<String> validator = Validators.matchesRegex(s -> s, "\\d+", "Muss nur Ziffern enthalten");
//        ValidationResult<String> result = validator.validate("12345");
//        assertThat(result.isValid()).isTrue();
//    }
//
//    @Test
//    public void matchesRegex_shouldReturnFailure_whenValueDoesNotMatch() {
//        Validator<String> validator = Validators.matchesRegex(s -> s, "\\d+", "Muss nur Ziffern enthalten");
//        ValidationResult<String> result = validator.validate("abc");
//        assertThat(result.isValid()).isFalse();
//        assertThat(result.getErrors()).containsExactly("Muss nur Ziffern enthalten");
//    }

    // --- Kombinierte Validatoren ---

//    @Test
//    public void all_shouldReturnSuccess_whenAllValidatorsSucceed() {
//        Validator<Integer> v1 = Validators.valueInRange(i -> i, 0, 10, "Fehler 1");
//        Validator<Integer> v2 = Validators.notNull(i -> i, "Fehler 2");
//        Validator<Integer> allValidator = Validators.all(v1, v2);
//
//        ValidationResult<Integer> result = allValidator.validate(5);
//        assertThat(result.isValid()).isTrue();
//    }

    // --- all Validatoren ---

    @Test
    public void all_shouldReturnFailure_whenOneValidatorFails() {
        Boundary left = Boundary.OPEN;
        Boundary right = Boundary.OPEN;

        Validator<Integer> v1 = Validators.valueInRange(i -> i, 0, 10, left, right,"Fehler 1");
        Validator<Integer> v2 = Validators.valueInRange(i -> i, 15, 20, left, right, "Fehler 2");
        Validator<Integer> allValidator = Validators.all(v1, v2);

        ValidationResult<Integer> result = allValidator.validate(5);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("Fehler 2");
    }

    @Test
    public void allNested_shouldReturnFailure_whenAddressIsInvalid() {
        // 1. Erstelle ein Objekt mit ungültigen Daten
        Address invalidAddress = new Address(" ", " ");
        Person validPerson = new Person("Jane Doe", 25, invalidAddress);

        // 2. Erstelle einen verschachtelten Validator für die Adresse, der bei dem ersten Fehler abbricht.
        Validator<Address> addressValidator = Validators.all(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // 3. Erstelle den Hauptvalidator, der ebenfalls bei dem ersten Fehler abbricht.
        Validator<Person> personValidator = Validators.all(
                Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
                Validators.numberInRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
                Validators.nested(Person::getAddress, addressValidator)
        );

        // 4. Führe die Validierung aus.
        ValidationResult<Person> result = personValidator.validate(validPerson);

        // 5. Überprüfe das Ergebnis.
        assertThat(result.isValid()).isFalse();
        // Die Validierung bricht beim ersten Fehler (Stadt darf nicht leer sein) ab.
        assertThat(result.getErrors()).containsExactly("Stadt darf nicht leer sein");
    }

    @Test
    public void allNested_shouldReturnFailure_whenAddressIsNull() {
        // 1. Erstelle ein Objekt, bei dem die verschachtelte Adresse null ist.
        Person personWithNullAddress = new Person("John Doe", 30, null);

        // 2. Erstelle einen verschachtelten Validator für die Adresse.
        // Dieser wird in diesem Fall nicht ausgeführt.
        Validator<Address> addressValidator = Validators.all(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // 3. Erstelle den Hauptvalidator für die Person.
        // Der notNull-Validator MUSS vor dem nested-Validator stehen.
        Validator<Person> personValidator = Validators.all(
                Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
                Validators.numberInRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
                Validators.notNull(Person::getAddress, "Adresse darf nicht null sein"), // <-- Hier ist die Null-Prüfung
                Validators.nested(Person::getAddress, addressValidator)
        );

        // 4. Führe die Validierung aus.
        ValidationResult<Person> result = personValidator.validate(personWithNullAddress);

        // 5. Überprüfe das Ergebnis.
        assertThat(result.isValid()).isFalse();
        // Die Validierung stoppt sofort beim ersten Fehler, der durch den notNull-Validator ausgelöst wird.
        assertThat(result.getErrors()).containsExactly("Adresse darf nicht null sein");
    }

    // --- allCollecting Validatoren ---

    @Test
    public void allCollectingNested_shouldReturnFailure_withAllErrors() {
        // 1. Erstelle ein Objekt mit ungültigen Daten, um mehrere Fehler zu erzeugen.
        // Der Name ist leer, und die Stadt sowie die PLZ sind ebenfalls leer.
        Address invalidAddress = new Address(" ", " ");
        Person invalidPerson = new Person(" ", 0, invalidAddress);

        // 2. Erstelle einen verschachtelten Validator für die Adresse, der alle Fehler sammelt.
        Validator<Address> addressValidator = Validators.allCollecting(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // 3. Erstelle den Hauptvalidator für die Person, der ebenfalls alle Fehler sammelt.
        // Er enthält eine Prüfung auf den Namen, das Alter und den verschachtelten Address-Validator.
        Validator<Person> personValidator = Validators.allCollecting(
                Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
                Validators.numberInRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
                Validators.nested(Person::getAddress, addressValidator)
        );

        // 4. Führe die Validierung aus.
        ValidationResult<Person> result = personValidator.validate(invalidPerson);

        // 5. Überprüfe die Ergebnisse.
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly(
                "Name darf nicht leer sein",
                "Alter muss zwischen 18 und 99 liegen",
                "Stadt darf nicht leer sein",
                "PLZ darf nicht leer sein"
        );
    }

    @Test
    public void allCollectingNested_shouldReturnFailure_whenNestedObjectIsNull() {
        // 1. Erstelle ein Objekt, bei dem die verschachtelte Adresse null ist.
        Person personWithNullAddress = new Person("John Doe", 30, null);

        // 2. Erstelle einen verschachtelten Validator für die Adresse, der alle Fehler sammelt.
        Validator<Address> addressValidator = Validators.allCollecting(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // 3. Erstelle den Hauptvalidator für die Person.
        // Der Knackpunkt: Füge einen notNull-Validator FÜR DIE ADRESSE hinzu,
        // der VOR dem verschachtelten Validator ausgeführt wird.
        Validator<Person> personValidator = Validators.allCollecting(
                Validators.notBlank(Person::getName, "Name darf nicht leer sein"),
                Validators.numberInRange(Person::getAge, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
                // Hier ist die Null-Prüfung. Sie wird als eigenständiger Fehler behandelt.
                Validators.notNull(Person::getAddress, "Adresse darf nicht null sein"),
                // Der verschachtelte Validator wird erst aufgerufen, wenn die Adresse nicht null ist.
                // Andernfalls würde die Kette aufgrund der notNull-Prüfung abgebrochen.
                Validators.nested(Person::getAddress, addressValidator)
        );

        // 4. Führe die Validierung aus.
        ValidationResult<Person> result = personValidator.validate(personWithNullAddress);

        // 5. Überprüfe die Ergebnisse.
        assertThat(result.isValid()).isFalse();
        // Es wird nur der Fehler "Adresse darf nicht null sein" gemeldet, da die nested-Prüfung
        // aufgrund der null-Adresse gar nicht erst ausgeführt wird.
        assertThat(result.getErrors()).containsExactly(
                "Adresse darf nicht null sein"
        );
    }

//    @Test
//    public void and_shouldReturnSuccess_whenBothValidatorsSucceed() {
//        Validator<Integer> v1 = Validators.inRange(i -> i, 0, 10, "Fehler 1");
//        Validator<Integer> v2 = Validators.inRange(i -> i, 5, 15, "Fehler 2");
//        Validator<Integer> andValidator = v1.and(v2);
//
//        ValidationResult<Integer> result = andValidator.validate(8);
//        assertThat(result.isValid()).isTrue();
//    }
//
//    @Test
//    public void or_shouldReturnSuccess_whenFirstValidatorSucceeds() {
//        Validator<Integer> v1 = Validators.inRange(i -> i, 0, 5, "Fehler 1");
//        Validator<Integer> v2 = Validators.inRange(i -> i, 10, 15, "Fehler 2");
//        Validator<Integer> orValidator = v1.or(v2);
//
//        ValidationResult<Integer> result = orValidator.validate(3);
//        assertThat(result.isValid()).isTrue();
//    }

//    @Test
//    public void or_shouldReturnFailure_whenBothValidatorsFail() {
//        Validator<Integer> v1 = Validators.inRange(i -> i, 0, 5, "Fehler 1");
//        Validator<Integer> v2 = Validators.inRange(i -> i, 10, 15, "Fehler 2");
//        Validator<Integer> orValidator = v1.or(v2);
//
//        ValidationResult<Integer> result = orValidator.validate(8);
//        assertThat(result.isValid()).isFalse();
//        assertThat(result.getErrors()).containsExactly("Fehler 1", "Fehler 2");
//    }

    // --- Verschachtelte Validatoren ---

    @Test
    public void nested_shouldReturnSuccess_whenNestedObjectIsValid() {
        Validator<Address> addressValidator = Validators.all(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // Kombinierte Validierung mit expliziter Null-Prüfung
        Validator<Person> personValidator = Validators.all(
                Validators.notBlank(Person::getName, "Der Name darf nicht leer sein"),
                Validators.notNull(Person::getAddress, "Adresse darf nicht null sein"),
                Validators.nested(Person::getAddress, addressValidator)
        );

        Person person = new Person("Anna", 30, new Address("Berlin", "10115"));
        ValidationResult<Person> result = personValidator.validate(person);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void nested_shouldReturnFailure_whenNestedObjectIsInvalid() {
        Validator<Address> addressValidator = Validators.all(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // Kombinierte Validierung mit expliziter Null-Prüfung
        Validator<Person> personValidator = Validators.all(
                Validators.notBlank(Person::getName, "Der Name darf nicht leer sein"),
                Validators.notNull(Person::getAddress, "Adresse darf nicht null sein"),
                Validators.nested(Person::getAddress, addressValidator)
        );

        Person person = new Person("Anna", 30, new Address("Berlin", ""));
        ValidationResult<Person> result = personValidator.validate(person);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("PLZ darf nicht leer sein");
    }

    @Test
    public void nested_shouldReturnFailure_whenNestedObjectIsNull() {
        Validator<Address> addressValidator = Validators.all(
                Validators.notBlank(Address::getCity, "Stadt darf nicht leer sein"),
                Validators.notBlank(Address::getZip, "PLZ darf nicht leer sein")
        );

        // Kombinierte Validierung mit expliziter Null-Prüfung
        Validator<Person> personValidator = Validators.all(
                Validators.notBlank(Person::getName, "Der Name darf nicht leer sein"),
                Validators.notNull(Person::getAddress, "Adresse darf nicht null sein"),
                Validators.nested(Person::getAddress, addressValidator)
        );

        Person person = new Person("Anna", 30, null);
        ValidationResult<Person> result = personValidator.validate(person);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("Adresse darf nicht null sein");
    }

    // --- Validatoren für Sammlungen ---

    @Test
    public void validateAllElements_shouldReturnSuccess_whenAllElementsAreValid() {
        Validator<String> notBlank = Validators.notBlank(s -> s, "String darf nicht leer sein");
        Validator<List<String>> listValidator = Validators.validateAllElements(notBlank);

        ValidationResult<List<String>> result = listValidator.validate(List.of("Hallo", "Welt"));
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validateAllElements_shouldReturnFailure_withIndexedErrors() {
        Validator<String> notBlank = Validators.notBlank(s -> s, "String darf nicht leer sein");
        Validator<List<String>> listValidator = Validators.validateAllElements(notBlank);

        ValidationResult<List<String>> result = listValidator.validate(List.of("Hallo", "", "Welt", " "));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly(
                "Fehler in Element 1: String darf nicht leer sein",
                "Fehler in Element 3: String darf nicht leer sein"
        );
    }

    @Test
    public void validateAllElementsNoIndex_shouldReturnFailure_withAggregatedErrors() {
        Validator<String> notBlank = Validators.notBlank(s -> s, "String darf nicht leer sein");
        Validator<List<String>> listValidator = Validators.validateAllElementsNoIndex(notBlank);

        ValidationResult<List<String>> result = listValidator.validate(List.of("Hallo", "", "Welt", " "));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly(
                "String darf nicht leer sein",
                "String darf nicht leer sein"
        );
    }
}
