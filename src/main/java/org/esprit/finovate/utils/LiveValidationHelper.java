package org.esprit.finovate.utils;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;
import javafx.scene.control.TextInputControl;

import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Binds live validation (contrôle de saisie) to controls while typing.
 * Updates field style and optional error label on each change.
 */
public final class LiveValidationHelper {

    private static final String ERROR_STYLE = "validation-error";

    private LiveValidationHelper() {}

    /**
     * Binds validation to a text field. Validates on every change.
     */
    public static void bind(TextField field, java.util.function.Function<String, String> validator) {
        bindTextInput(field, validator);
    }

    /**
     * Binds validation to a password field.
     */
    public static void bind(PasswordField field, java.util.function.Function<String, String> validator) {
        bindTextInput(field, validator);
    }

    /**
     * Binds validation to a text area.
     */
    public static void bind(TextArea field, java.util.function.Function<String, String> validator) {
        bindTextInput(field, validator);
    }

    /**
     * Binds validation to a date picker. Validates on value change.
     */
    public static void bind(DatePicker picker, java.util.function.Function<LocalDate, String> validator) {
        ChangeListener<LocalDate> listener = (obs, oldVal, newVal) -> {
            String err = validator.apply(newVal);
            applyStyleClass(picker, err != null);
        };
        picker.valueProperty().addListener(listener);
        listener.changed(null, null, picker.getValue());
    }

    /**
     * Binds validation with an optional error label below the field.
     */
    public static void bind(TextField field, Label errorLabel,
                           java.util.function.Function<String, String> validator) {
        bindTextInputWithLabel(field, errorLabel, validator);
    }

    public static void bind(PasswordField field, Label errorLabel,
                           java.util.function.Function<String, String> validator) {
        bindTextInputWithLabel(field, errorLabel, validator);
    }

    public static void bind(TextArea field, Label errorLabel,
                           java.util.function.Function<String, String> validator) {
        bindTextInputWithLabel(field, errorLabel, validator);
    }

    /**
     * Binds amount validation that may depend on a dynamic max (e.g. remaining to fund).
     */
    public static void bindAmount(TextField field, Supplier<Double> maxAmountSupplier) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            String err = ValidationUtils.validateInvestmentAmount(
                    newVal == null ? "" : newVal.trim(),
                    maxAmountSupplier.get()
            );
            applyStyle(field, err != null);
        });
        field.focusedProperty().addListener((obs, wasFocused, nowFocused) -> {
            if (!nowFocused) {
                String err = ValidationUtils.validateInvestmentAmount(
                        field.getText() == null ? "" : field.getText().trim(),
                        maxAmountSupplier.get()
                );
                applyStyle(field, err != null);
            }
        });
    }

    private static void bindTextInput(TextInputControl field, java.util.function.Function<String, String> validator) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            String err = validator.apply(newVal == null ? "" : newVal);
            applyStyle(field, err != null);
        });
        field.focusedProperty().addListener((obs, wasFocused, nowFocused) -> {
            if (!nowFocused) {
                String err = validator.apply(field.getText() == null ? "" : field.getText());
                applyStyle(field, err != null);
            }
        });
    }

    private static void bindTextInputWithLabel(TextInputControl field, Label errorLabel,
                                               java.util.function.Function<String, String> validator) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            String err = validator.apply(newVal == null ? "" : newVal);
            applyStyleAndLabel(field, errorLabel, err);
        });
        field.focusedProperty().addListener((obs, wasFocused, nowFocused) -> {
            if (!nowFocused) {
                String err = validator.apply(field.getText() == null ? "" : field.getText());
                applyStyleAndLabel(field, errorLabel, err);
            }
        });
    }

    private static void applyStyle(TextInputControl field, boolean hasError) {
        applyStyleClass(field, hasError);
    }

    private static void applyStyleClass(Control control, boolean hasError) {
        if (hasError) {
            if (!control.getStyleClass().contains(ERROR_STYLE)) control.getStyleClass().add(ERROR_STYLE);
        } else {
            control.getStyleClass().removeAll(ERROR_STYLE);
        }
    }

    private static void applyStyleAndLabel(Control field, Label errorLabel, String error) {
        boolean hasError = error != null;
        if (field instanceof TextInputControl) {
            if (hasError) {
                if (!field.getStyleClass().contains(ERROR_STYLE)) field.getStyleClass().add(ERROR_STYLE);
            } else {
                field.getStyleClass().removeAll(ERROR_STYLE);
            }
        }
        if (errorLabel != null) {
            errorLabel.setText(hasError ? error : "");
            errorLabel.setVisible(hasError);
            errorLabel.setManaged(hasError);
        }
    }

    /**
     * Clears validation error style from a control (e.g. on form reset).
     */
    public static void clearError(Control control) {
        control.getStyleClass().removeAll(ERROR_STYLE);
    }

    /**
     * Clears validation error from multiple controls.
     */
    public static void clearAllErrors(Control... controls) {
        for (Control c : controls) clearError(c);
    }
}
