package org.example.utils;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour ValidationUtils
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ValidationUtilsTest {

    // ==================== TESTS EMAIL VALIDATION ====================

    @Test
    @Order(1)
    @DisplayName("Devrait valider un email correct")
    void isValidEmail_WithValidEmail_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidEmail("test@example.com")).isTrue();
        assertThat(ValidationUtils.isValidEmail("user.name@domain.co.uk")).isTrue();
        assertThat(ValidationUtils.isValidEmail("user+tag@example.com")).isTrue();
        assertThat(ValidationUtils.isValidEmail("user_name@example.com")).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Devrait rejeter un email sans @")
    void isValidEmail_WithoutAtSign_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidEmail("testexample.com")).isFalse();
        assertThat(ValidationUtils.isValidEmail("test.example.com")).isFalse();
    }

    @Test
    @Order(3)
    @DisplayName("Devrait rejeter un email avec format invalide")
    void isValidEmail_WithInvalidFormat_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidEmail("@example.com")).isFalse();
        assertThat(ValidationUtils.isValidEmail("test@")).isFalse();
        assertThat(ValidationUtils.isValidEmail("test@.com")).isFalse();
        assertThat(ValidationUtils.isValidEmail("")).isFalse();
        assertThat(ValidationUtils.isValidEmail(null)).isFalse();
        assertThat(ValidationUtils.isValidEmail("test @example.com")).isFalse();
    }

    // ==================== TESTS USERNAME VALIDATION ====================

    @Test
    @Order(4)
    @DisplayName("Devrait valider un username correct")
    void isValidUsername_WithValidUsername_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidUsername("john_doe")).isTrue();
        assertThat(ValidationUtils.isValidUsername("user123")).isTrue();
        assertThat(ValidationUtils.isValidUsername("User_Name_123")).isTrue();
        assertThat(ValidationUtils.isValidUsername("abc")).isTrue(); // 3 caractères minimum
    }

    @Test
    @Order(5)
    @DisplayName("Devrait rejeter un username trop court")
    void isValidUsername_TooShort_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidUsername("ab")).isFalse();
        assertThat(ValidationUtils.isValidUsername("a")).isFalse();
        assertThat(ValidationUtils.isValidUsername("")).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("Devrait rejeter un username trop long")
    void isValidUsername_TooLong_ShouldReturnFalse() {
        String longUsername = "a".repeat(51);
        assertThat(ValidationUtils.isValidUsername(longUsername)).isFalse();
    }

    @Test
    @Order(7)
    @DisplayName("Devrait rejeter un username avec caractères spéciaux")
    void isValidUsername_WithSpecialChars_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidUsername("user@name")).isFalse();
        assertThat(ValidationUtils.isValidUsername("user name")).isFalse();
        assertThat(ValidationUtils.isValidUsername("user-name")).isFalse();
        assertThat(ValidationUtils.isValidUsername("user.name")).isFalse();
        assertThat(ValidationUtils.isValidUsername(null)).isFalse();
    }

    // ==================== TESTS PASSWORD VALIDATION ====================

    @Test
    @Order(8)
    @DisplayName("Devrait valider un password correct")
    void isValidPassword_WithValidPassword_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidPassword("password123")).isTrue();
        assertThat(ValidationUtils.isValidPassword("123456")).isTrue();
        assertThat(ValidationUtils.isValidPassword("abcdef")).isTrue();
        assertThat(ValidationUtils.isValidPassword("P@ssw0rd!")).isTrue();
    }

    @Test
    @Order(9)
    @DisplayName("Devrait rejeter un password trop court")
    void isValidPassword_TooShort_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidPassword("12345")).isFalse();
        assertThat(ValidationUtils.isValidPassword("abc")).isFalse();
        assertThat(ValidationUtils.isValidPassword("")).isFalse();
    }

    @Test
    @Order(10)
    @DisplayName("Devrait rejeter un password null")
    void isValidPassword_Null_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidPassword(null)).isFalse();
    }

    // ==================== TESTS FORUM NAME VALIDATION ====================

    @Test
    @Order(11)
    @DisplayName("Devrait valider un nom de forum correct")
    void isValidForumName_WithValidName_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidForumName("Crypto Trading")).isTrue();
        assertThat(ValidationUtils.isValidForumName("ABC")).isTrue(); // 3 caractères minimum
        assertThat(ValidationUtils.isValidForumName("A".repeat(100))).isTrue(); // 100 caractères maximum
    }

    @Test
    @Order(12)
    @DisplayName("Devrait rejeter un nom de forum trop court")
    void isValidForumName_TooShort_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidForumName("AB")).isFalse();
        assertThat(ValidationUtils.isValidForumName("")).isFalse();
        assertThat(ValidationUtils.isValidForumName("  ")).isFalse(); // Espaces seulement
    }

    @Test
    @Order(13)
    @DisplayName("Devrait rejeter un nom de forum trop long")
    void isValidForumName_TooLong_ShouldReturnFalse() {
        String longName = "A".repeat(101);
        assertThat(ValidationUtils.isValidForumName(longName)).isFalse();
    }

    // ==================== TESTS DESCRIPTION VALIDATION ====================

    @Test
    @Order(14)
    @DisplayName("Devrait valider une description correcte")
    void isValidDescription_WithValidDescription_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidDescription("This is a valid description")).isTrue();
        assertThat(ValidationUtils.isValidDescription("A".repeat(10))).isTrue(); // 10 caractères minimum
        assertThat(ValidationUtils.isValidDescription("A".repeat(1000))).isTrue(); // 1000 caractères maximum
    }

    @Test
    @Order(15)
    @DisplayName("Devrait rejeter une description trop courte")
    void isValidDescription_TooShort_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidDescription("Short")).isFalse();
        assertThat(ValidationUtils.isValidDescription("")).isFalse();
    }

    @Test
    @Order(16)
    @DisplayName("Devrait rejeter une description trop longue")
    void isValidDescription_TooLong_ShouldReturnFalse() {
        String longDescription = "A".repeat(1001);
        assertThat(ValidationUtils.isValidDescription(longDescription)).isFalse();
    }

    // ==================== TESTS POST TITLE VALIDATION ====================

    @Test
    @Order(17)
    @DisplayName("Devrait valider un titre de post correct")
    void isValidPostTitle_WithValidTitle_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidPostTitle("Valid Post Title")).isTrue();
        assertThat(ValidationUtils.isValidPostTitle("ABCDE")).isTrue(); // 5 caractères minimum
        assertThat(ValidationUtils.isValidPostTitle("A".repeat(200))).isTrue(); // 200 caractères maximum
    }

    @Test
    @Order(18)
    @DisplayName("Devrait rejeter un titre de post trop court")
    void isValidPostTitle_TooShort_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidPostTitle("Bad")).isFalse();
        assertThat(ValidationUtils.isValidPostTitle("")).isFalse();
    }

    @Test
    @Order(19)
    @DisplayName("Devrait rejeter un titre de post trop long")
    void isValidPostTitle_TooLong_ShouldReturnFalse() {
        String longTitle = "A".repeat(201);
        assertThat(ValidationUtils.isValidPostTitle(longTitle)).isFalse();
    }

    // ==================== TESTS POST CONTENT VALIDATION ====================

    @Test
    @Order(20)
    @DisplayName("Devrait valider un contenu de post correct")
    void isValidPostContent_WithValidContent_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidPostContent("This is a valid post content")).isTrue();
        assertThat(ValidationUtils.isValidPostContent("A".repeat(10))).isTrue(); // 10 caractères minimum
        assertThat(ValidationUtils.isValidPostContent("A".repeat(5000))).isTrue(); // 5000 caractères maximum
    }

    @Test
    @Order(21)
    @DisplayName("Devrait rejeter un contenu de post trop court")
    void isValidPostContent_TooShort_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidPostContent("Short")).isFalse();
        assertThat(ValidationUtils.isValidPostContent("")).isFalse();
    }

    @Test
    @Order(22)
    @DisplayName("Devrait rejeter un contenu de post trop long")
    void isValidPostContent_TooLong_ShouldReturnFalse() {
        String longContent = "A".repeat(5001);
        assertThat(ValidationUtils.isValidPostContent(longContent)).isFalse();
    }

    // ==================== TESTS COMMENT CONTENT VALIDATION ====================

    @Test
    @Order(23)
    @DisplayName("Devrait valider un contenu de commentaire correct")
    void isValidCommentContent_WithValidContent_ShouldReturnTrue() {
        assertThat(ValidationUtils.isValidCommentContent("Valid comment")).isTrue();
        assertThat(ValidationUtils.isValidCommentContent("A")).isTrue(); // 1 caractère minimum
        assertThat(ValidationUtils.isValidCommentContent("A".repeat(1000))).isTrue(); // 1000 caractères maximum
    }

    @Test
    @Order(24)
    @DisplayName("Devrait rejeter un contenu de commentaire vide")
    void isValidCommentContent_Empty_ShouldReturnFalse() {
        assertThat(ValidationUtils.isValidCommentContent("")).isFalse();
        assertThat(ValidationUtils.isValidCommentContent("   ")).isFalse(); // Espaces seulement
        assertThat(ValidationUtils.isValidCommentContent(null)).isFalse();
    }

    @Test
    @Order(25)
    @DisplayName("Devrait rejeter un contenu de commentaire trop long")
    void isValidCommentContent_TooLong_ShouldReturnFalse() {
        String longComment = "A".repeat(1001);
        assertThat(ValidationUtils.isValidCommentContent(longComment)).isFalse();
    }

    // ==================== TESTS SANITIZATION ====================

    @Test
    @Order(26)
    @DisplayName("Devrait supprimer les espaces en début et fin")
    void sanitize_WithSpaces_ShouldTrimThem() {
        assertThat(ValidationUtils.sanitize("  Hello World  ")).isEqualTo("Hello World");
        assertThat(ValidationUtils.sanitize("\tTabbed\t")).isEqualTo("Tabbed");
        assertThat(ValidationUtils.sanitize("\nNewline\n")).isEqualTo("Newline");
    }

    @Test
    @Order(27)
    @DisplayName("Devrait conserver le texte normal")
    void sanitize_WithNormalText_ShouldKeepIt() {
        assertThat(ValidationUtils.sanitize("Hello World")).isEqualTo("Hello World");
        assertThat(ValidationUtils.sanitize("Test123")).isEqualTo("Test123");
    }

    @Test
    @Order(28)
    @DisplayName("Devrait retourner chaîne vide pour null")
    void sanitize_WithNull_ShouldReturnEmptyString() {
        assertThat(ValidationUtils.sanitize(null)).isEqualTo("");
    }
}
