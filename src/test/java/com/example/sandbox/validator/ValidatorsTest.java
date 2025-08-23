package com.example.sandbox.validator;

import org.junit.Test;

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

    @Test
    public void dateInFuture_shouldReturnSuccess_whenDateIsInFuture() {
        Validator<LocalDate> validator = Validators.dateInFuture(d -> d, "Datum muss in der Zukunft liegen");
        ValidationResult<LocalDate> result = validator.validate(LocalDate.now().plusDays(1));
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
        Validator<Person> personValidator = Validators.nested(Person::getAddress, addressValidator, "Adresse ungültig");

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
        Validator<Person> personValidator = Validators.nested(Person::getAddress, addressValidator, "Adresse ungültig");

        Person person = new Person("Anna", 30, new Address("Berlin", ""));
        ValidationResult<Person> result = personValidator.validate(person);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("PLZ darf nicht leer sein");
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


    // --- Hilfsklassen für die Tests ---

    static class Person {
        private String name;
        private int age;
        private Address address;

        public Person(String name, int age, Address address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() { return name; }
        public Address getAddress() { return address; }
    }

    static class Address {
        private String city;
        private String zip;

        public Address(String city, String zip) {
            this.city = city;
            this.zip = zip;
        }

        public String getCity() { return city; }
        public String getZip() { return zip; }
    }
}