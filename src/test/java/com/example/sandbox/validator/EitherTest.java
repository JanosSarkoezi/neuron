package com.example.sandbox.validator;

import com.example.sandbox.triade.Either;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testklasse für das Either-Interface.
 * Nutzt JUnit 4.13 und AssertJ für die Asserts.
 */
public class EitherTest {

    // ---
    // Factory-Methoden Tests

    @Test
    public void testLeftFactoryMethod() {
        Either<String, Integer> either = Either.left("Fehler");
        assertThat(either).isInstanceOf(Either.Left.class);
    }

    @Test
    public void testRightFactoryMethod() {
        Either<String, Integer> either = Either.right(42);
        assertThat(either).isInstanceOf(Either.Right.class);
    }

    // ---
    // fold() Methode Tests

    @Test
    public void testFoldOnRight() {
        Either<String, Integer> either = Either.right(10);
        String result = either.fold(
                error -> "Fehler: " + error,
                value -> "Erfolg: " + (value * 2)
        );
        assertThat(result).isEqualTo("Erfolg: 20");
    }

    @Test
    public void testFoldOnLeft() {
        Either<String, Integer> either = Either.left("Ungültige Eingabe");
        String result = either.fold(
                error -> "Fehler: " + error,
                value -> "Erfolg: " + (value * 2)
        );
        assertThat(result).isEqualTo("Fehler: Ungültige Eingabe");
    }

    // ---
    // map() Methode Tests

    @Test
    public void testMapOnRight() {
        Either<String, Integer> either = Either.right(5);
        Either<String, String> mapped = either.map(Object::toString);
        assertThat(mapped).isInstanceOf(Either.Right.class);
        String value = mapped.fold(left -> null, right -> right);
        assertThat(value).isEqualTo("5");
    }

    @Test
    public void testMapOnLeft() {
        Either<String, Integer> either = Either.left("Fehler!");
        Either<String, String> mapped = either.map(Object::toString);
        assertThat(mapped).isInstanceOf(Either.Left.class);
        String value = mapped.fold(left -> left, right -> null);
        assertThat(value).isEqualTo("Fehler!");
    }

    // ---
    // mapLeft() Methode Tests

    @Test
    public void testMapLeftOnLeft() {
        Either<Integer, String> either = Either.left(500);
        Either<String, String> mappedLeft = either.mapLeft(code -> "Fehlercode: " + code);
        assertThat(mappedLeft).isInstanceOf(Either.Left.class);
        String value = mappedLeft.fold(left -> left, right -> null);
        assertThat(value).isEqualTo("Fehlercode: 500");
    }

    @Test
    public void testMapLeftOnRight() {
        Either<Integer, String> either = Either.right("Success");
        Either<String, String> mappedLeft = either.mapLeft(code -> "Fehlercode: " + code);
        assertThat(mappedLeft).isInstanceOf(Either.Right.class);
        String value = mappedLeft.fold(left -> null, right -> right);
        assertThat(value).isEqualTo("Success");
    }

    // ---
    // flatMap() Methode Tests

    private Either<String, Integer> parseNumber(String s) {
        try {
            return Either.right(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Either.left("Ungültige Zahl: " + s);
        }
    }

    @Test
    public void testFlatMapOnRightSuccess() {
        Either<String, String> either = Either.right("123");
        Either<String, Integer> result = either.flatMap(this::parseNumber);
        assertThat(result).isInstanceOf(Either.Right.class);
        Integer value = result.fold(left -> null, right -> right);
        assertThat(value).isEqualTo(123);
    }

    @Test
    public void testFlatMapOnRightFailure() {
        Either<String, String> either = Either.right("abc");
        Either<String, Integer> result = either.flatMap(this::parseNumber);
        assertThat(result).isInstanceOf(Either.Left.class);
        String value = result.fold(left -> left, right -> null);
        assertThat(value).isEqualTo("Ungültige Zahl: abc");
    }

    @Test
    public void testFlatMapOnLeft() {
        Either<String, String> either = Either.left("Initialer Fehler");
        Either<String, Integer> result = either.flatMap(this::parseNumber);
        assertThat(result).isInstanceOf(Either.Left.class);
        String value = result.fold(left -> left, right -> null);
        assertThat(value).isEqualTo("Initialer Fehler");
    }

    // ---
    // Hilfsmethoden Tests

    @Test
    public void testToOptionalOnRight() {
        Either<String, Integer> either = Either.right(42);
        Optional<Integer> optional = either.toOptional();
        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo(42);
    }

    @Test
    public void testToOptionalOnLeft() {
        Either<String, Integer> either = Either.left("Fehler");
        Optional<Integer> optional = either.toOptional();
        assertThat(optional).isEmpty();
    }

    @Test
    public void testGetOrElseOnRight() {
        Either<String, Integer> either = Either.right(100);
        int result = either.getOrElse(0);
        assertThat(result).isEqualTo(100);
    }

    @Test
    public void testGetOrElseOnLeft() {
        Either<String, Integer> either = Either.left("Fehler");
        int result = either.getOrElse(0);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void testGetOrThrowOnRight() {
        Either<String, Integer> either = Either.right(99);

        // Es wird kein Fehler geworfen, da es ein Right ist.
        // Der Supplier-Lambda wird nie ausgeführt.
        int result = either.getOrThrow(msg -> new RuntimeException("Dieser Fehler wird nicht geworfen"));

        assertThat(result).isEqualTo(99);
    }

    @Test
    public void testGetOrThrowOnLeft() {
        Either<String, Integer> either = Either.left("Ein Fehler ist aufgetreten.");

        // Wir erwarten, dass eine RuntimeException geworfen wird.
        // Die Lambda-Funktion stellt die Ausnahme mit der korrekten Nachricht bereit.
        assertThatThrownBy(() -> either.getOrThrow(RuntimeException::new))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ein Fehler ist aufgetreten.");
    }
}