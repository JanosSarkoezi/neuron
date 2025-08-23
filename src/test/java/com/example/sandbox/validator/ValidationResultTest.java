package com.example.sandbox.validator;

import org.junit.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ValidationResultTest {

    // --- Tests für Factory-Methoden ---

    @Test
    public void success_shouldCreateValidResultWithValue() {
        ValidationResult<String> result = ValidationResult.success("Daten");
        assertThat(result.isValid()).isTrue();
        assertThat(result.toOptional()).contains("Daten");
    }

    @Test
    public void failure_withSingleMessage_shouldCreateInvalidResult() {
        ValidationResult<String> result = ValidationResult.failure("Fehler 1");
        assertThat(result.isValid()).isFalse();
        assertThat(result.toOptional()).isEmpty();
    }

    @Test
    public void failure_withMultipleMessages_shouldCreateInvalidResult() {
        ValidationResult<String> result = ValidationResult.failure(List.of("Fehler 1", "Fehler 2"));
        assertThat(result.isValid()).isFalse();
        assertThat(result.toOptional()).isEmpty();
    }

    // --- Tests für Basismethoden ---

    @Test
    public void getOrElse_shouldReturnValue_onSuccess() {
        ValidationResult<String> result = ValidationResult.success("Erfolg");
        assertThat(result.getOrElse("Standard")).isEqualTo("Erfolg");
    }

    @Test
    public void getOrElse_shouldReturnDefaultValue_onFailure() {
        ValidationResult<String> result = ValidationResult.failure("Fehler");
        assertThat(result.getOrElse("Standard")).isEqualTo("Standard");
    }

    @Test
    public void toOptional_shouldReturnPresentOptional_onSuccess() {
        ValidationResult<String> result = ValidationResult.success("Wert");
        Optional<String> optional = result.toOptional();
        assertThat(optional).isPresent();
        assertThat(optional.get()).isEqualTo("Wert");
    }

    @Test
    public void toOptional_shouldReturnEmptyOptional_onFailure() {
        ValidationResult<String> result = ValidationResult.failure("Fehler");
        Optional<String> optional = result.toOptional();
        assertThat(optional).isEmpty();
    }

    // --- Tests für funktionale Transformationen ---

    @Test
    public void map_shouldTransformValue_onSuccess() {
        ValidationResult<Integer> result = ValidationResult.success(10);
        ValidationResult<String> mappedResult = result.map(String::valueOf);

        assertThat(mappedResult.isValid()).isTrue();
        assertThat(mappedResult.getOrElse(null)).isEqualTo("10");
    }

    @Test
    public void map_shouldNotTransform_onFailure() {
        ValidationResult<Integer> result = ValidationResult.failure("Fehler beim Mappen");
        ValidationResult<String> mappedResult = result.map(String::valueOf);

        assertThat(mappedResult.isValid()).isFalse();

        List<String> errors = mappedResult.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Fehler beim Mappen");
    }

    @Test
    public void flatMap_shouldApplyNewValidation_onSuccess() {
        ValidationResult<String> result = ValidationResult.success("123");
        ValidationResult<Integer> flatMappedResult = result.flatMap(s -> ValidationResult.success(Integer.valueOf(s)));

        assertThat(flatMappedResult.isValid()).isTrue();
        assertThat(flatMappedResult.getOrElse(null)).isEqualTo(123);
    }

    @Test
    public void flatMap_shouldReturnFailure_whenMapperFails() {
        ValidationResult<String> result = ValidationResult.success("abc");
        ValidationResult<Integer> flatMappedResult = result.flatMap(s -> ValidationResult.failure("Keine Zahl"));

        assertThat(flatMappedResult.isValid()).isFalse();

        List<String> errors = flatMappedResult.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Keine Zahl");
    }

    @Test
    public void flatMap_shouldNotApplyNewValidation_onInitialFailure() {
        ValidationResult<String> result = ValidationResult.failure("Initialer Fehler");
        ValidationResult<Integer> flatMappedResult = result.flatMap(s -> ValidationResult.success(Integer.valueOf(s)));

        assertThat(flatMappedResult.isValid()).isFalse();

        List<String> errors = flatMappedResult.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Initialer Fehler");
    }

    // --- Tests für Kombinations- und Fold-Methoden ---

    @Test
    public void fold_shouldExecuteOnSuccessFunction_onSuccess() {
        ValidationResult<String> result = ValidationResult.success("OK");
        String foldedValue = result.fold(
                s -> "Gefaltet: " + s,
                errors -> "Fehler: " + errors.get(0)
        );
        assertThat(foldedValue).isEqualTo("Gefaltet: OK");
    }

    @Test
    public void fold_shouldExecuteOnFailureFunction_onFailure() {
        ValidationResult<String> result = ValidationResult.failure("Nicht OK");
        String foldedValue = result.fold(
                s -> "Gefaltet: " + s,
                errors -> "Fehler: " + errors.get(0)
        );
        assertThat(foldedValue).isEqualTo("Fehler: Nicht OK");
    }

    @Test
    public void combine_shouldReturnFailure_whenSecondIsFail() {
        ValidationResult<String> r1 = ValidationResult.success("1");
        ValidationResult<String> r2 = ValidationResult.failure("Fehler 2");

        ValidationResult<String> combined = r1.combine(r2);

        assertThat(combined.isValid()).isFalse();
        List<String> errors = combined.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Fehler 2");
    }

    @Test
    public void combine_shouldReturnCombinedErrors_whenBothFail() {
        ValidationResult<String> r1 = ValidationResult.failure("Fehler 1");
        ValidationResult<String> r2 = ValidationResult.failure("Fehler 2");

        ValidationResult<String> combined = r1.combine(r2);

        assertThat(combined.isValid()).isFalse();

        List<String> errors = combined.fold(v -> null, e -> e);
        assertThat(errors).containsExactly("Fehler 1", "Fehler 2");
    }

    // --- Tests für Peek-Methode ---

    @Test
    public void peek_shouldCallOnSuccessConsumer_onSuccess() {
        final boolean[] successCalled = {false};
        ValidationResult<String> result = ValidationResult.success("Wert");

        ValidationResult<String> peekedResult = result.peek(
                s -> successCalled[0] = true,
                errors -> {}
        );

        assertThat(successCalled[0]).isTrue();
        assertThat(peekedResult).isSameAs(result);
    }

    @Test
    public void peek_shouldCallOnFailureConsumer_onFailure() {
        final boolean[] failureCalled = {false};
        ValidationResult<String> result = ValidationResult.failure("Fehler");

        ValidationResult<String> peekedResult = result.peek(
                s -> {},
                errors -> failureCalled[0] = true
        );

        assertThat(failureCalled[0]).isTrue();
        assertThat(peekedResult).isSameAs(result);
    }
}