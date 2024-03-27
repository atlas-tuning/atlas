package com.github.manevolent.atlas.ui.settings.validation;

import com.github.manevolent.atlas.ui.settings.SettingPage;

import java.util.ArrayList;
import java.util.List;

public class ValidationState {
    private final List<ValidationProblem> problems = new ArrayList<>();

    public void addProblem(ValidationProblem problem) {
        problems.add(problem);
    }

    public void add(SettingPage page, ValidationSeverity severity, String errorMessage) {
        addProblem(new ValidationProblem(page, severity, errorMessage));
    }

    public boolean willBlockApply() {
        return problems.stream().anyMatch(problem -> problem.getSeverity().willBlockApply());
    }

    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    public List<ValidationProblem> getProblems() {
        return new ArrayList<>(problems);
    }
}
