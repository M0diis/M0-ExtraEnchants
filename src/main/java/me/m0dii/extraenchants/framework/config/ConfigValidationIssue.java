package me.m0dii.extraenchants.framework.config;

import java.util.Objects;

public record ConfigValidationIssue(
        Severity severity,
        String file,
        String path,
        String message
) {
    public ConfigValidationIssue {
        severity = Objects.requireNonNull(severity, "severity");
        file = file == null ? "<unknown>" : file;
        path = path == null ? "" : path;
        message = message == null ? "" : message;
    }

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    public String format() {
        String location = path.isBlank() ? file : file + ":" + path;
        return "[" + severity.name() + "] " + location + " - " + message;
    }

    public enum Severity {
        ERROR,
        WARNING
    }
}
