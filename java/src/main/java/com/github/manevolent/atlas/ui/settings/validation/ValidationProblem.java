package com.github.manevolent.atlas.ui.settings.validation;

import com.github.manevolent.atlas.ui.settings.SettingPage;
import com.github.manevolent.atlas.ui.settings.field.SettingField;

public class ValidationProblem {
    private final SettingPage page;
    private final ValidationSeverity severity;
    private final String errorMessage;
    private SettingField<?> field;


    public ValidationProblem(SettingPage page, ValidationSeverity severity, String errorMessage) {
        this.page = page;
        this.severity = severity;
        this.errorMessage = errorMessage;
    }

    public SettingPage getPage() {
        return page;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public SettingField<?> getField() {
        return field;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public void setField(SettingField<?> field) {
        this.field = field;
    }
}
