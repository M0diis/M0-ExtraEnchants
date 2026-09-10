package me.m0dii.extraenchants.framework.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class CustomEnchantConfigSchema {
    public static final int CURRENT_VERSION = 1;
    public static final int LEGACY_VERSION = 0;

    private final Map<Integer, Consumer<YamlConfiguration>> migrations = new TreeMap<>();

    public CustomEnchantConfigSchema() {
        // Version 0 is the format used by the original bundled YAML files.
        // Keeping this as a real hook makes the next schema change explicit.
        registerMigration(LEGACY_VERSION, ignored -> {
        });
    }

    public CustomEnchantConfigSchema registerMigration(int fromVersion, Consumer<YamlConfiguration> migration) {
        if (fromVersion < 0 || fromVersion >= CURRENT_VERSION) {
            throw new IllegalArgumentException("Migration source version must be between 0 and "
                    + (CURRENT_VERSION - 1));
        }

        migrations.put(fromVersion, Objects.requireNonNull(migration, "migration"));
        return this;
    }

    public int readVersion(YamlConfiguration yaml, Consumer<ConfigValidationIssue> issueConsumer, String fileName) {
        Object rawVersion = yaml.get("schema-version");
        if (rawVersion == null) {
            issueConsumer.accept(new ConfigValidationIssue(
                    ConfigValidationIssue.Severity.WARNING,
                    fileName,
                    "schema-version",
                    "Missing schema-version; assuming legacy schema " + LEGACY_VERSION
            ));
            return LEGACY_VERSION;
        }

        if (!(rawVersion instanceof Number number)
                || number.doubleValue() != Math.rint(number.doubleValue())) {
            issueConsumer.accept(new ConfigValidationIssue(
                    ConfigValidationIssue.Severity.ERROR,
                    fileName,
                    "schema-version",
                    "Expected an integer schema version"
            ));
            return CURRENT_VERSION;
        }

        int version = number.intValue();
        if (version < LEGACY_VERSION) {
            issueConsumer.accept(new ConfigValidationIssue(
                    ConfigValidationIssue.Severity.ERROR,
                    fileName,
                    "schema-version",
                    "Schema version cannot be negative"
            ));
            return CURRENT_VERSION;
        }

        if (version > CURRENT_VERSION) {
            issueConsumer.accept(new ConfigValidationIssue(
                    ConfigValidationIssue.Severity.ERROR,
                    fileName,
                    "schema-version",
                    "Unsupported schema version " + version + "; current version is " + CURRENT_VERSION
            ));
        }

        return version;
    }

    public boolean migrate(YamlConfiguration yaml, int fromVersion, Consumer<ConfigValidationIssue> issueConsumer,
                           String fileName) {
        if (fromVersion > CURRENT_VERSION) {
            return false;
        }

        int version = fromVersion;
        while (version < CURRENT_VERSION) {
            Consumer<YamlConfiguration> migration = migrations.get(version);
            if (migration == null) {
                issueConsumer.accept(new ConfigValidationIssue(
                        ConfigValidationIssue.Severity.ERROR,
                        fileName,
                        "schema-version",
                        "No migration hook is registered for schema version " + version
                ));
                return false;
            }

            try {
                migration.accept(yaml);
            } catch (RuntimeException ex) {
                issueConsumer.accept(new ConfigValidationIssue(
                        ConfigValidationIssue.Severity.ERROR,
                        fileName,
                        "schema-version",
                        "Schema migration from version " + version + " failed: " + ex.getMessage()
                ));
                return false;
            }
            version++;
        }

        return true;
    }
}
