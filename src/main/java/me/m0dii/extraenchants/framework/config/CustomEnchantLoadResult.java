package me.m0dii.extraenchants.framework.config;

import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CustomEnchantLoadResult {
    private final Map<String, CustomEnchantDefinition> definitions;
    private final List<ConfigValidationIssue> issues;
    private final int filesRead;
    private final int validFiles;
    private final boolean committed;

    public CustomEnchantLoadResult(
            Map<String, CustomEnchantDefinition> definitions,
            List<ConfigValidationIssue> issues,
            int filesRead,
            int validFiles,
            boolean committed
    ) {
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
        this.issues = List.copyOf(issues);
        this.filesRead = filesRead;
        this.validFiles = validFiles;
        this.committed = committed;
    }

    public Map<String, CustomEnchantDefinition> getDefinitions() {
        return definitions;
    }

    public List<ConfigValidationIssue> getIssues() {
        return issues;
    }

    public int getFilesRead() {
        return filesRead;
    }

    public int getValidFiles() {
        return validFiles;
    }

    public int getDefinitionCount() {
        return definitions.size();
    }

    public long getErrorCount() {
        return issues.stream().filter(ConfigValidationIssue::isError).count();
    }

    public long getWarningCount() {
        return issues.stream().filter(issue -> !issue.isError()).count();
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(ConfigValidationIssue::isError);
    }

    public boolean isValid() {
        return !hasErrors();
    }

    /**
     * True when the returned definitions represent the complete directory,
     * rather than a partially parsed configuration.
     */
    public boolean isCommitted() {
        return committed;
    }
}
