package me.m0dii.extraenchants.framework.config;

import me.m0dii.extraenchants.framework.registry.ConditionRegistry;
import me.m0dii.extraenchants.framework.runtime.Exp4jFormulaEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomEnchantConfigLoaderTest {
    @TempDir
    Path directory;

    @Test
    void loadsFilesInDeterministicOrder() throws Exception {
        write("zeta.yml", validYaml("zeta", "AXE"));
        write("Alpha.yml", validYaml("alpha", "SWORD"));

        CustomEnchantLoadResult result = loader().loadResult(directory.toFile());

        assertTrue(result.isCommitted());
        assertEquals(List.of("alpha", "zeta"), List.copyOf(result.getDefinitions().keySet()));
        assertEquals(2, result.getFilesRead());
        assertEquals(2, result.getValidFiles());
    }

    @Test
    void rejectsUnknownKeysAndDoesNotReturnPartialDefinitions() throws Exception {
        write("valid.yml", validYaml("valid", "SWORD"));
        write("invalid.yml", validYaml("invalid", "SWORD") + "unknown-root: true\n");

        CustomEnchantLoadResult result = loader().loadResult(directory.toFile());

        assertFalse(result.isCommitted());
        assertTrue(result.hasErrors());
        assertTrue(result.getDefinitions().isEmpty());
    }

    @Test
    void rejectsUnsupportedSchemaVersion() throws Exception {
        write("future.yml", validYaml("future", "SWORD").replace("schema-version: 1", "schema-version: 99"));

        CustomEnchantLoadResult result = loader().loadResult(directory.toFile());

        assertFalse(result.isCommitted());
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.path().equals("schema-version")));
    }

    @Test
    void bundledExamplesPassStrictValidation() {
        CustomEnchantLoadResult result = loader().loadResult(
                Path.of("src", "main", "resources", "custom-enchants").toFile());

        assertTrue(result.isCommitted(), result.getIssues().toString());
        assertEquals(23, result.getDefinitionCount());
    }

    private CustomEnchantConfigLoader loader() {
        return new CustomEnchantConfigLoader(
                null,
                new ConditionParser(new ConditionRegistry(), new Exp4jFormulaEngine()));
    }

    private void write(String name, String yaml) throws Exception {
        Files.writeString(directory.resolve(name), yaml);
    }

    private String validYaml(String id, String item) {
        return "schema-version: 1\n"
                + "id: " + id + "\n"
                + "display-name: '&f" + id + "'\n"
                + "max-level: 3\n"
                + "applicable-items:\n"
                + "  - " + item + "\n"
                + "triggers:\n"
                + "  onAttack:\n"
                + "    chance: '100'\n"
                + "    effects:\n"
                + "      - damage: 'level + 1'\n";
    }
}
