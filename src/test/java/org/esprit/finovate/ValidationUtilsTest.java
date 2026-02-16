package org.esprit.finovate;

import org.esprit.finovate.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValidationUtils} (contrôle de saisie).
 */
public class ValidationUtilsTest {

    @Test
    void validateRequired_emptyOrNull_returnsError() {
        assertNotNull(ValidationUtils.validateRequired(null, "Field"));
        assertNotNull(ValidationUtils.validateRequired("", "Field"));
        assertNotNull(ValidationUtils.validateRequired("  ", "Field"));
        assertNull(ValidationUtils.validateRequired("value", "Field"));
    }

    @Test
    void validateLength_validRange_returnsNull() {
        assertNull(ValidationUtils.validateLength("ab", 2, 10, "Name"));
        assertNull(ValidationUtils.validateLength("hello", 1, 5, "Name"));
    }

    @Test
    void validateLength_invalid_returnsError() {
        assertNotNull(ValidationUtils.validateLength("a", 2, 10, "Name"));
        assertNotNull(ValidationUtils.validateLength("123456789012345678901234567890", 1, 20, "Name"));
    }

    @Test
    void validateAmount_valid_returnsNull() {
        assertNull(ValidationUtils.validateAmount("100", 0.01, 1000, "Amount"));
        assertNull(ValidationUtils.validateAmount("100.50", 0.01, 10000, "Amount"));
        assertNull(ValidationUtils.validateAmount("100,50", 0.01, 10000, "Amount"));
    }

    @Test
    void validateAmount_invalid_returnsError() {
        assertNotNull(ValidationUtils.validateAmount("", 0.01, 1000, "Amount"));
        assertNotNull(ValidationUtils.validateAmount("0", 0.01, 1000, "Amount"));
        assertNotNull(ValidationUtils.validateAmount("abc", 0.01, 1000, "Amount"));
    }

    @Test
    void validateName_valid_returnsNull() {
        assertNull(ValidationUtils.validateName("Ali", "First name"));
        assertNull(ValidationUtils.validateName("Ben Foulen", "Last name"));
        assertNull(ValidationUtils.validateName("Jean-Pierre", "Name"));
    }

    @Test
    void validateName_invalid_returnsError() {
        assertNotNull(ValidationUtils.validateName("A", "Name"));
        assertNotNull(ValidationUtils.validateName("", "Name"));
        assertNotNull(ValidationUtils.validateName("123", "Name"));
    }

    @Test
    void validateDeadline_future_returnsNull() {
        assertNull(ValidationUtils.validateDeadline(LocalDate.now().plusDays(1), "Deadline"));
        assertNull(ValidationUtils.validateDeadline(LocalDate.now(), "Deadline"));
    }

    @Test
    void validateDeadline_past_returnsError() {
        assertNotNull(ValidationUtils.validateDeadline(LocalDate.now().minusDays(1), "Deadline"));
    }

    @Test
    void validateEmail_valid_returnsNull() {
        assertNull(ValidationUtils.validateEmail("user@example.com"));
        assertNull(ValidationUtils.validateEmail("test.user@finovate.tn"));
    }

    @Test
    void validateEmail_invalid_returnsError() {
        assertNotNull(ValidationUtils.validateEmail(""));
        assertNotNull(ValidationUtils.validateEmail("invalid"));
        assertNotNull(ValidationUtils.validateEmail("@domain.com"));
    }

    @Test
    void validatePassword_valid_returnsNull() {
        assertNull(ValidationUtils.validatePassword("abc"));
        assertNull(ValidationUtils.validatePassword("password123"));
    }

    @Test
    void validatePassword_invalid_returnsError() {
        assertNotNull(ValidationUtils.validatePassword(null));
        assertNotNull(ValidationUtils.validatePassword(""));
        assertNotNull(ValidationUtils.validatePassword("ab"));
    }

    @Test
    void validateTitle_valid_returnsNull() {
        assertNull(ValidationUtils.validateTitle("My Project"));
    }

    @Test
    void validateTitle_invalid_returnsError() {
        assertNotNull(ValidationUtils.validateTitle(""));
        assertNotNull(ValidationUtils.validateTitle(null));
    }

    @Test
    void parseAmount_valid() {
        assertEquals(100.5, ValidationUtils.parseAmount("100.5"), 0.001);
        assertEquals(100.5, ValidationUtils.parseAmount("100,5"), 0.001);
    }

    @Test
    void validateAll_allPass_returnsNull() {
        String err = ValidationUtils.validateAll(
                () -> ValidationUtils.validateRequired("a", "A"),
                () -> ValidationUtils.validateRequired("b", "B")
        );
        assertNull(err);
    }

    @Test
    void validateAll_firstFails_returnsFirstError() {
        String err = ValidationUtils.validateAll(
                () -> ValidationUtils.validateRequired("", "A"),
                () -> ValidationUtils.validateRequired("b", "B")
        );
        assertNotNull(err);
        assertTrue(err.contains("A"));
    }
}
