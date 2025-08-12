package com.example.sandbox.validator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;

// Angenommen, Sie haben eine einfache User-Klasse für die on()-Tests
public class ValidatorTest {

    // --- Tests für Factory-Methoden ---

    @Test
    public void of_shouldReturnSuccess_whenPredicateIsTrue() {
        Validator<String> isNotNull = Validator.of(Objects::nonNull, "Cannot be null");
        ValidationResult<String> result = isNotNull.validate("hello");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getOrElse(null)).isEqualTo("hello");
    }

    @Test
    public void of_shouldReturnFailure_whenPredicateIsFalse() {
        Validator<String> isNotNull = Validator.of(Objects::nonNull, "Cannot be null");
        ValidationResult<String> result = isNotNull.validate(null);

        assertThat(result.isValid()).isFalse();

        List<String> errors = result.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Cannot be null");
    }

    @Test
    public void from_shouldReturnCorrectResult() {
        Validator<String> startsWithA = Validator.from(s -> {
            if (s != null && s.startsWith("A")) {
                return ValidationResult.success(s);
            }
            return ValidationResult.failure("Must start with 'A'");
        });

        ValidationResult<String> successResult = startsWithA.validate("Apple");
        assertThat(successResult.isValid()).isTrue();

        ValidationResult<String> failureResult = startsWithA.validate("Banana");
        assertThat(failureResult.isValid()).isFalse();

        List<String> errors = failureResult.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Must start with 'A'");
    }

    // --- Tests für Kombinationsmethoden ---

    @Test
    public void and_shouldReturnFirstFailure_onFailure() {
        Validator<String> notNull = Validator.of(Objects::nonNull, "Cannot be null");
        Validator<String> notEmpty = Validator.of(s -> !s.isEmpty(), "Cannot be empty");

        Validator<String> notNullAndNotEmpty = notNull.and(notEmpty);

        ValidationResult<String> result = notNullAndNotEmpty.validate(null);

        assertThat(result.isValid()).isFalse();

        List<String> errors = result.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Cannot be null");
    }

    @Test
    public void and_shouldReturnSuccess_whenBothSucceed() {
        Validator<String> notNull = Validator.of(Objects::nonNull, "Cannot be null");
        Validator<String> notEmpty = Validator.of(s -> !s.isEmpty(), "Cannot be empty");

        Validator<String> notNullAndNotEmpty = notNull.and(notEmpty);

        ValidationResult<String> result = notNullAndNotEmpty.validate("test");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getOrElse(null)).isEqualTo("test");
    }

    @Test
    public void or_shouldReturnFirstSuccess_onSuccess() {
        Validator<String> isCat = Validator.of(s -> "Cat".equals(s), "Not a cat");
        Validator<String> isDog = Validator.of(s -> "Dog".equals(s), "Not a dog");

        Validator<String> isCatOrDog = isCat.or(isDog);

        ValidationResult<String> result = isCatOrDog.validate("Cat");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOrElse(null)).isEqualTo("Cat");
    }

    @Test
    public void or_shouldReturnCombinedErrors_whenBothFail() {
        Validator<String> isCat = Validator.of(s -> "Cat".equals(s), "Not a cat");
        Validator<String> isDog = Validator.of(s -> "Dog".equals(s), "Not a dog");

        Validator<String> isCatOrDog = isCat.or(isDog);

        ValidationResult<String> result = isCatOrDog.validate("Bird");
        assertThat(result.isValid()).isFalse();

        List<String> errors = result.fold(v -> null, e -> e);
        assertThat(errors).containsExactlyInAnyOrder("Not a cat", "Not a dog");
    }

    @Test
    public void not_shouldInvertResult_fromSuccessToFailure() {
        Validator<String> isEmpty = Validator.of(String::isEmpty, "Is not empty");
        Validator<String> notEmpty = isEmpty.not("Is empty");

        ValidationResult<String> result = notEmpty.validate("");
        assertThat(result.isValid()).isFalse();

        List<String> errors = result.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Is empty");
    }

    @Test
    public void not_shouldInvertResult_fromFailureToSuccess() {
        Validator<String> isEmpty = Validator.of(String::isEmpty, "Is not empty");
        Validator<String> notEmpty = isEmpty.not("Is empty");

        ValidationResult<String> result = notEmpty.validate("test");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getOrElse(null)).isEqualTo("test");
    }

    // --- Tests für Abbildungsfunktionen ---

    @Test
    public void on_shouldValidateExtractedValue_andReturnOriginalObject() {
        Validator<String> nameValidator = Validator.of(s -> s.length() > 2, "Name too short");
        Validator<User> userValidator = nameValidator.on(User::getName);

        User user = new User("Jo", "test@test.de", 30);
        ValidationResult<User> result = userValidator.validate(user);

        assertThat(result.isValid()).isFalse();

        List<String> errors = result.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Name too short");
    }

    @Test
    public void onIfPresent_shouldSucceed_whenExtractedValueIsNull() {
        Validator<Integer> ageValidator = Validator.of(age -> age > 18, "Too young");
        Validator<User> userValidator = ageValidator.onIfPresent(User::getAge);

        User user = new User("John", "john@test.de", null);
        ValidationResult<User> result = userValidator.validate(user);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getOrElse(null)).isEqualTo(user);
    }

    @Test
    public void onIfPresent_shouldFail_whenExtractedValueIsPresentAndInvalid() {
        Validator<Integer> ageValidator = Validator.of(age -> age > 18, "Too young");
        Validator<User> userValidator = ageValidator.onIfPresent(User::getAge);

        User user = new User("John", "john@test.de", 15);
        ValidationResult<User> result = userValidator.validate(user);

        assertThat(result.isValid()).isFalse();

        List<String> errors = result.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Too young");
    }
}