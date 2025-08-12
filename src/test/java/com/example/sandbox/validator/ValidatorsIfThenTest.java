package com.example.sandbox.validator;

import org.junit.jupiter.api.Test;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;
import static com.example.sandbox.validator.Validators.ifThen;

public class ValidatorsIfThenTest {

    private static final String PRECONDITION_ERROR = "Precondition failed: Value is null.";
    private static final String CONSEQUENCE_ERROR = "Consequence failed: Value is blank.";

    // Ein Validator, der fehlschlägt, wenn der Wert null ist.
    private final Validator<String> notNullValidator = Validator.of(
            Objects::nonNull, PRECONDITION_ERROR);

    // Ein Validator, der fehlschlägt, wenn der Wert leer ist.
    private final Validator<String> notBlankValidator = Validator.of(
            s -> !s.isBlank(), CONSEQUENCE_ERROR);

    @Test
    public void whenPreconditionFails_shouldReturnPreconditionErrors() {
        // Arrange
        // Die Vorbedingung 'notNull' wird mit 'null' aufgerufen und sollte fehlschlagen.
        Validator<String> conditionalValidator = ifThen(notNullValidator, notBlankValidator);

        // Act
        ValidationResult<String> result = conditionalValidator.validate(null);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly(PRECONDITION_ERROR);
    }

    @Test
    public void whenPreconditionPassesButConsequenceFails_shouldReturnConsequenceErrors() {
        // Arrange
        // Die Vorbedingung 'notNull' wird mit "" aufgerufen und sollte erfolgreich sein.
        // Die Konsequenz 'notBlank' sollte mit "" aufgerufen werden und fehlschlagen.
        Validator<String> conditionalValidator = ifThen(notNullValidator, notBlankValidator);

        // Act
        ValidationResult<String> result = conditionalValidator.validate(" ");

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly(CONSEQUENCE_ERROR);
    }

    @Test
    public void whenPreconditionAndConsequencePass_shouldReturnSuccess() {
        // Arrange
        // Die Vorbedingung 'notNull' und die Konsequenz 'notBlank' sollten beide erfolgreich sein.
        Validator<String> conditionalValidator = ifThen(notNullValidator, notBlankValidator);

        // Act
        ValidationResult<String> result = conditionalValidator.validate("test");

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.toOptional()).hasValue("test");
    }
}