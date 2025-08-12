# Java-Validierungsbibliothek

Eine leichtgewichtige, funktionale Validierungsbibliothek für Java. Sie bietet eine flexible und intuitive Möglichkeit, Geschäftsregeln zu definieren und zu kombinieren, um die Datenintegrität in Ihrer Anwendung sicherzustellen.

## Kernkonzepte

### `ValidationResult<T>`

Das zentrale Ergebnis-Objekt, das das Resultat einer Validierung darstellt. Es kann entweder ein `Success<T>` sein, das den validierten Wert enthält, oder ein `Failure<T>`, das eine Liste von Fehlermeldungen speichert. Das "Sealed Interface"-Muster sorgt dafür, dass nur diese beiden Zustände möglich sind, was die Handhabung mittels `switch`-Expressions erleichtert.

### `Validator<T>`

Ein funktionales Interface, das die Validierungslogik kapselt. Es nimmt ein Objekt des Typs `T` entgegen und gibt ein `ValidationResult<T>` zurück. Dank seiner flüssigen API (`and`, `or`, `not`, `on`) lassen sich einfache Validatoren wie Bausteine zu komplexen Regeln zusammensetzen.

### `Validators`

Eine Utility-Klasse, die eine Sammlung von statischen Factory-Methoden für die Erstellung gebräuchlicher Validierungsregeln bereitstellt. Hier finden sich nützliche Helfer wie `notNull`, `notBlank`, `isTrue` oder auch komplexere wie `all` und `ifThen`.

-----

## Anwendungsbeispiele

### Einfache Validierung

Erstelle einen grundlegenden `Validator` mithilfe der `of`-Methode.

```java
import com.example.sandbox.validator.Validator;
import com.example.sandbox.validator.ValidationResult;

public class SimpleValidationExample {
    public static void main(String[] args) {
        Validator<String> notEmptyValidator = Validator.of(
            s -> !s.isEmpty(), "Input must not be empty.");

        ValidationResult<String> success = notEmptyValidator.validate("Hello");
        System.out.println("Success: " + success.isValid()); // true

        ValidationResult<String> failure = notEmptyValidator.validate("");
        System.out.println("Failure: " + failure.isValid()); // false
        System.out.println("Errors: " + failure.getErrors()); // [Input must not be empty.]
    }
}
```

### Kombinierte Validierung mit `and` und `or`

Verketten Sie mehrere Validatoren, um komplexere Regeln zu erstellen.

```java
package com.example.sandbox.validator.example;

import com.example.sandbox.validator.Validator;
import com.example.sandbox.validator.Validators;
import com.example.sandbox.validator.ValidationResult;

public class CombinedValidationExample {
    public static void main(String[] args) {
        // Kombination von Validatoren mit 'and'
        Validator<String> strongPasswordValidatorAll = Validators.all(
                Validators.hasMinLength(8, "Password must be at least 8 characters long."),
                Validators.containsDigit("Password must contain a digit."),
                Validators.containsSpecialChar("Password must contain a special character.")
        );

        ValidationResult<String> result1 = strongPasswordValidatorAll.validate("test123!");
        System.out.println("Valid Password: " + result1.isValid()); // true

        ValidationResult<String> result2 = strongPasswordValidatorAll.validate("pass");
        System.out.println("Invalid Password: " + result2.getErrors());
        // [Password must be at least 8 characters long.]

        // Kombination von Validatoren mit 'or'
        Validator<String> strongPasswordValidatorAny = Validators.anyOf(
                Validators.hasMinLength(8, "Password must be at least 8 characters long."),
                Validators.containsDigit("Password must contain a digit."),
                Validators.containsSpecialChar("Password must contain a special character.")
        );

        result1 = strongPasswordValidatorAny.validate("test123!");
        System.out.println("Valid Password: " + result1.isValid()); // true

        result2 = strongPasswordValidatorAny.validate("pass");
        System.out.println("Invalid Password: " + result2.getErrors());
        // [Password must be at least 8 characters long., Password must contain a digit., Password must contain a special character.]
    }
}
```

### Verschachtelte Validierung mit `nested`

Auch auf verschachtelte Objekte kann zugegriffen werden. Hier hat eine Person eine Adresse. Die Person und die Adresse können validiert werden.

```java
import com.example.sandbox.validator.ValidationResult;
import com.example.sandbox.validator.Validator;
import com.example.sandbox.validator.Validators;

// Dummy-Klassen für das Beispiel. In einer echten Anwendung wären diese in separaten Dateien.
record Address(String street, String city, String zip) {}
record Person(String name, int age, Address address) {}

public class PersonValidationExample {

    public static void main(String[] args) {
        // Der Validator, der alle Regeln kombiniert.
        Validator<Person> personValidator = Validators.all(
            Validators.notBlank(Person::name, "Name darf nicht leer sein"),
            Validators.numberInRange(Person::age, 18, 99, "Alter muss zwischen 18 und 99 liegen"),
            // Die `nested`-Methode benötigt einen nullMessage-Parameter.
            Validators.nested(
                Person::address,
                Validators.all(
                    Validators.notBlank(Address::city, "Stadt darf nicht leer sein"),
                    Validators.notBlank(Address::zip, "PLZ darf nicht leer sein")
                ),
                "Adresse darf nicht null sein" // Hier der fehlende Parameter.
            )
        );

        // --- Beispiel 1: Gültiges Objekt ---
        System.out.println("--- Validierung eines gültigen Objekts ---");
        Person validPerson = new Person("Anna", 30, new Address("Hauptstraße 1", "Berlin", "12345"));
        ValidationResult<Person> result1 = personValidator.validate(validPerson);

        System.out.println("Ist gültig: " + result1.isValid()); // true
        result1.getErrors().forEach(System.out::println);
        System.out.println();

        // --- Beispiel 2: Ungültiges Objekt ---
        System.out.println("--- Validierung eines ungültigen Objekts ---");
        Person invalidPerson = new Person("Tim", 15, new Address("", "Hamburg", null));
        ValidationResult<Person> result2 = personValidator.validate(invalidPerson);

        System.out.println("Ist gültig: " + result2.isValid()); // false
        System.out.println("Fehler:");
        result2.getErrors().forEach(System.out::println);
    }
}
```

### Bedingte Validierung mit `ifThen`

Wenden Sie eine Validierungsregel nur an, wenn eine bestimmte Vorbedingung erfüllt ist.

```java
import com.example.sandbox.validator.Validator;
import com.example.sandbox.validator.Validators;
import com.example.sandbox.validator.ValidationResult;

public class ConditionalValidationExample {
    public static void main(String[] args) {
        // Wenn das Land "USA" ist, muss die Postleitzahl 5 Ziffern haben.
        Validator<User> zipCodeValidator = Validators.ifThen(
            Validators.isTrue(u -> u.getCountry().equals("USA"), "User is not from USA."),
            Validators.matchesRegex(u -> u.getZipCode(), "\\d{5}", "US zip code must be 5 digits.")
        );
        
        // Annahme: User-Klasse existiert
        User user1 = new User("USA", "12345");
        System.out.println("Valid user: " + zipCodeValidator.validate(user1).isValid()); // true
        
        User user2 = new User("USA", "123");
        System.out.println("Invalid user: " + zipCodeValidator.validate(user2).getErrors());
        // [US zip code must be 5 digits.]
        
        User user3 = new User("DE", "123");
        System.out.println("Valid (precondition not met): " + zipCodeValidator.validate(user3).isValid()); // true
    }
}

// Dummy-Klasse für das Beispiel
class User {
    private final String country;
    private final String zipCode;
    // Konstruktor, Getter, etc.
    public User(String country, String zipCode) {
        this.country = country;
        this.zipCode = zipCode;
    }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }
}
```
