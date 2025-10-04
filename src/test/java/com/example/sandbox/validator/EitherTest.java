package com.example.sandbox.validator;

import com.example.sandbox.triade.Either;
import com.example.sandbox.triade.ThrowingFunction;
import com.example.sandbox.triade.ThrowingSupplier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testklasse für das Either-Interface.
 * Nutzt JUnit 5 und AssertJ für die Asserts.
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

    @Test
    public void testPureFactoryMethod() {
        Either<String, Integer> either = Either.pure(42);
        assertThat(either).isInstanceOf(Either.Right.class);
        assertThat(either.getOrElse(0)).isEqualTo(42);
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
    // match() Methode Tests

    @Test
    public void testMatchOnRight() {
        List<String> results = new ArrayList<>();
        Either<String, Integer> either = Either.right(10);
        either.match(
                error -> results.add("left"),
                value -> results.add("right:" + value)
        );
        assertThat(results).containsExactly("right:10");
    }

    @Test
    public void testMatchOnLeft() {
        List<String> results = new ArrayList<>();
        Either<String, Integer> either = Either.left("error");
        either.match(
                error -> results.add("left:" + error),
                value -> results.add("right")
        );
        assertThat(results).containsExactly("left:error");
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
    // bimap() Methode Tests

    @Test
    public void testBimapOnRight() {
        Either<String, Integer> either = Either.right(10);
        Either<Integer, String> bimapped = either.bimap(String::length, Object::toString);
        assertThat(bimapped).isInstanceOf(Either.Right.class);
        assertThat(bimapped.getOrElse("")).isEqualTo("10");
    }

    @Test
    public void testBimapOnLeft() {
        Either<String, Integer> either = Either.left("error");
        Either<Integer, String> bimapped = either.bimap(String::length, Object::toString);
        assertThat(bimapped).isInstanceOf(Either.Left.class);
        assertThat(bimapped.toOptionalLeft()).hasValue(5);
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
    // ap() Methode Tests

    @Test
    public void testApOnRight() {
        Either<String, Function<Integer, String>> eitherFn = Either.right(i -> "Result: " + i);
        Either<String, Integer> eitherValue = Either.right(42);
        Either<String, String> result = eitherValue.<String>ap(eitherFn);
        assertThat(result).isInstanceOf(Either.Right.class);
        assertThat(result.getOrElse("")).isEqualTo("Result: 42");
    }

    @Test
    public void testApOnLeftValue() {
        Either<String, Function<Integer, String>> eitherFn = Either.right(i -> "Result: " + i);
        Either<String, Integer> eitherValue = Either.left("value error");
        Either<String, String> result = eitherValue.<String>ap(eitherFn);
        assertThat(result).isInstanceOf(Either.Left.class);
        assertThat(result.toOptionalLeft()).hasValue("value error");
    }

    @Test
    public void testApOnLeftFunction() {
        Either<String, Function<Integer, String>> eitherFn = Either.left("function error");
        Either<String, Integer> eitherValue = Either.right(42);
        Either<String, String> result = eitherValue.<String>ap(eitherFn);
        assertThat(result).isInstanceOf(Either.Left.class);
        assertThat(result.toOptionalLeft()).hasValue("function error");
    }

    // ---
    // swap() Methode Tests

    @Test
    public void testSwapOnRight() {
        Either<String, Integer> either = Either.right(42);
        Either<Integer, String> swapped = either.swap();
        assertThat(swapped).isInstanceOf(Either.Left.class);
        assertThat(swapped.toOptionalLeft()).hasValue(42);
    }

    @Test
    public void testSwapOnLeft() {
        Either<String, Integer> either = Either.left("error");
        Either<Integer, String> swapped = either.swap();
        assertThat(swapped).isInstanceOf(Either.Right.class);
        assertThat(swapped.getOrElse("")).isEqualTo("error");
    }

    // ---
    // Hilfsmethoden Tests

    @Test
    public void testIsLeftAndIsRight() {
        Either<String, Integer> left = Either.left("error");
        assertThat(left.isLeft()).isTrue();
        assertThat(left.isRight()).isFalse();

        Either<String, Integer> right = Either.right(42);
        assertThat(right.isLeft()).isFalse();
        assertThat(right.isRight()).isTrue();
    }

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
    public void testToOptionalLeftOnLeft() {
        Either<String, Integer> either = Either.left("error");
        Optional<String> optional = either.toOptionalLeft();
        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo("error");
    }

    @Test
    public void testToOptionalLeftOnRight() {
        Either<String, Integer> either = Either.right(42);
        Optional<String> optional = either.toOptionalLeft();
        assertThat(optional).isEmpty();
    }

    @Test
    public void testStreamOnRight() {
        Either<String, Integer> either = Either.right(42);
        List<Integer> list = either.stream().collect(Collectors.toList());
        assertThat(list).containsExactly(42);
    }

    @Test
    public void testStreamOnLeft() {
        Either<String, Integer> either = Either.left("error");
        List<Integer> list = either.stream().collect(Collectors.toList());
        assertThat(list).isEmpty();
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
    public void testOrElseGetOnRight() {
        Either<String, Integer> either = Either.right(100);
        int result = either.orElseGet(() -> 0);
        assertThat(result).isEqualTo(100);
    }

    @Test
    public void testOrElseGetOnLeft() {
        Either<String, Integer> either = Either.left("Fehler");
        int result = either.orElseGet(() -> 0);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void testGetOrThrowOnRight() {
        Either<String, Integer> either = Either.right(99);
        int result = either.getOrThrow(msg -> new RuntimeException("Dieser Fehler wird nicht geworfen"));
        assertThat(result).isEqualTo(99);
    }

    @Test
    public void testGetOrThrowOnLeft() {
        Either<String, Integer> either = Either.left("Ein Fehler ist aufgetreten.");
        assertThatThrownBy(() -> either.getOrThrow(RuntimeException::new))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ein Fehler ist aufgetreten.");
    }

    // ---
    // Peeking Tests

    @Test
    public void testPeekOnRight() {
        List<Integer> peeked = new ArrayList<>();
        Either<String, Integer> either = Either.right(42);
        Either<String, Integer> result = either.peek(peeked::add);
        assertThat(peeked).containsExactly(42);
        assertThat(result).isSameAs(either);
    }

    @Test
    public void testPeekOnLeft() {
        List<Integer> peeked = new ArrayList<>();
        Either<String, Integer> either = Either.left("error");
        either.peek(peeked::add);
        assertThat(peeked).isEmpty();
    }

    @Test
    public void testPeekLeftOnLeft() {
        List<String> peeked = new ArrayList<>();
        Either<String, Integer> either = Either.left("error");
        Either<String, Integer> result = either.peekLeft(peeked::add);
        assertThat(peeked).containsExactly("error");
        assertThat(result).isSameAs(either);
    }

    @Test
    public void testPeekLeftOnRight() {
        List<String> peeked = new ArrayList<>();
        Either<String, Integer> either = Either.right(42);
        either.peekLeft(peeked::add);
        assertThat(peeked).isEmpty();
    }

    // ---
    // Filter Tests

    @Test
    public void testFilterOrElseOnRightWhenPredicateSucceeds() {
        Either<String, Integer> either = Either.right(10);
        Either<String, Integer> filtered = either.filterOrElse(n -> n > 5, () -> "zu klein");
        assertThat(filtered).isInstanceOf(Either.Right.class);
        assertThat(filtered.getOrElse(0)).isEqualTo(10);
    }

    @Test
    public void testFilterOrElseOnRightWhenPredicateFails() {
        Either<String, Integer> either = Either.right(3);
        Either<String, Integer> filtered = either.filterOrElse(n -> n > 5, () -> "zu klein");
        assertThat(filtered).isInstanceOf(Either.Left.class);
        assertThat(filtered.toOptionalLeft()).hasValue("zu klein");
    }

    @Test
    public void testFilterOrElseOnLeft() {
        Either<String, Integer> either = Either.left("error");
        Either<String, Integer> filtered = either.filterOrElse(n -> n > 5, () -> "zu klein");
        assertThat(filtered).isInstanceOf(Either.Left.class);
        assertThat(filtered.toOptionalLeft()).hasValue("error");
    }

    // ---
    // Sequenzierung Tests

    @Test
    public void testSequenceWithAllRights() {
        List<Either<String, Integer>> eithers = List.of(Either.right(1), Either.right(2), Either.right(3));
        Either<String, List<Integer>> result = Either.sequence(eithers);
        assertThat(result).isInstanceOf(Either.Right.class);
        assertThat(result.getOrElse(List.of())).containsExactly(1, 2, 3);
    }

    @Test
    public void testSequenceWithOneLeft() {
        List<Either<String, Integer>> eithers = List.of(Either.right(1), Either.left("error"), Either.right(3));
        Either<String, List<Integer>> result = Either.sequence(eithers);
        assertThat(result).isInstanceOf(Either.Left.class);
        assertThat(result.toOptionalLeft()).hasValue("error");
    }

    @Test
    public void testSequenceAccumulateWithAllRights() {
        List<Either<String, Integer>> eithers = List.of(Either.right(1), Either.right(2), Either.right(3));
        Either<List<String>, List<Integer>> result = Either.sequenceAccumulate(eithers);
        assertThat(result).isInstanceOf(Either.Right.class);
        assertThat(result.getOrElse(List.of())).containsExactly(1, 2, 3);
    }

    @Test
    public void testSequenceAccumulateWithMultipleLefts() {
        List<Either<String, Integer>> eithers = List.of(Either.right(1), Either.left("error1"), Either.right(3), Either.left("error2"));
        Either<List<String>, List<Integer>> result = Either.sequenceAccumulate(eithers);
        assertThat(result).isInstanceOf(Either.Left.class);
        assertThat(result.toOptionalLeft().orElse(List.of())).containsExactly("error1", "error2");
    }

    // ---
    // Catching Tests

    @Test
    public void testCatchingSupplierSuccess() {
        Either<Throwable, Integer> result = Either.catching(() -> 42);
        assertThat(result).isInstanceOf(Either.Right.class);
        assertThat(result.getOrElse(0)).isEqualTo(42);
    }

    @Test
    public void testCatchingSupplierFailure() {
        RuntimeException ex = new RuntimeException("boom");
        Either<Throwable, Integer> result = Either.catching(() -> {
            throw ex;
        });
        assertThat(result).isInstanceOf(Either.Left.class);
        assertThat(result.toOptionalLeft()).hasValue(ex);
    }

    @Test
    public void testCatchingSupplierWithMapperSuccess() {
        Either<String, Integer> result = Either.catching(() -> 42, Throwable::getMessage);
        assertThat(result).isInstanceOf(Either.Right.class);
        assertThat(result.getOrElse(0)).isEqualTo(42);
    }

    @Test
    public void testCatchingSupplierWithMapperFailure() {
        RuntimeException ex = new RuntimeException("boom");
        ThrowingSupplier<Integer> supplier = () -> {
            throw ex;
        };
        Function<Throwable, String> mapper = Throwable::getMessage;

        Either<String, Integer> result = Either.catching(supplier, mapper);

        assertThat(result).isInstanceOf(Either.Left.class);
        assertThat(result.toOptionalLeft()).hasValue("boom");
    }

    @Test
    void testCatchingFunctionSuccess() {
        ThrowingFunction<String, Integer> parser = Integer::parseInt;
        Either<Throwable, Integer> result = Either.catchingValue("42", parser);
        assertThat(result.getOrElse(0)).isEqualTo(42);
    }

    @Test
    void testCatchingFunctionFailure() {
        Either<Throwable, Integer> result = Either.catchingValue("abc", Integer::parseInt);
        assertThat(result.isLeft()).isTrue();
        assertThat(result.toOptionalLeft().get()).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void testCatchingFunctionWithMapperFailure() {
        Either<String, Integer> result = Either.catching("abc", Integer::parseInt, t -> "Fehler!");
        assertThat(result.toOptionalLeft()).hasValue("Fehler!");
    }

    @Test
    void testInstanceCatchingSuccess() {
        Either<String, String> either = Either.right("42");
        ThrowingFunction<String, Integer> fn = Integer::parseInt;
        Function<Throwable, String> errorMapper = Throwable::getMessage;
        Either<String, Integer> result = either.catching(fn, errorMapper);
        assertThat(result.getOrElse(0)).isEqualTo(42);
    }

    @Test
    void testInstanceCatchingFailure() {
        Either<String, String> either = Either.right("abc");
        ThrowingFunction<String, Integer> fn = Integer::parseInt;
        Function<Throwable, String> errorMapper = t -> "Fehler!";
        Either<String, Integer> result = either.catching(fn, errorMapper);
        assertThat(result.toOptionalLeft()).hasValue("Fehler!");
    }

    @Test
    void testInstanceCatchingOnLeft() {
        Either<String, String> either = Either.left("original");
        ThrowingFunction<String, Integer> fn = Integer::parseInt;
        Function<Throwable, String> errorMapper = t -> "Fehler!";
        Either<String, Integer> result = either.catching(fn, errorMapper);
        assertThat(result.toOptionalLeft()).hasValue("original");
    }

    // ---
    // Lifting Tests

    @Test
    public void testLift() {
        Function<Integer, String> intToString = Object::toString;
        Function<Either<String, Integer>, Either<String, String>> lifted = Either.lift(intToString);

        Either<String, Integer> right = Either.right(42);
        Either<String, String> resultRight = lifted.apply(right);
        assertThat(resultRight.getOrElse("")).isEqualTo("42");

        Either<String, Integer> left = Either.left("error");
        Either<String, String> resultLeft = lifted.apply(left);
        assertThat(resultLeft.toOptionalLeft()).hasValue("error");
    }
}