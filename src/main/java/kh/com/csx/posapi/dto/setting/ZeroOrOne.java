package kh.com.csx.posapi.dto.setting;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ZeroOrOne.ZeroOrOneValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ZeroOrOne {

    String message() default "Value must be either 0 (No) or 1 (Yes).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class ZeroOrOneValidator implements ConstraintValidator<ZeroOrOne, Integer> {
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            return value == null || value == 0 || value == 1;
        }

        @Override
        public void initialize(ZeroOrOne constraintAnnotation) {
            // Initialization code if necessary
        }
    }
}
