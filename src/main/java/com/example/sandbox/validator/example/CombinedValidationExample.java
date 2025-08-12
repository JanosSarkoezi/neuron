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