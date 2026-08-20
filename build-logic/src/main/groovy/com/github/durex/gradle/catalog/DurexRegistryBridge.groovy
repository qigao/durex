package com.github.durex.gradle.catalog

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

final class DurexRegistryBridge {
    static final int EXPECTED_SCHEMA_VERSION = 1

    static DependencyCatalogSnapshot fromProject(Project project) {
        def registration = project.gradle.sharedServices.registrations.findByName('durexDependencyRegistry')
        if (registration == null) {
            fail('durexDependencyRegistry is not available; apply durex.settings in this build')
        }

        Object service = registration.service.get()
        Object raw
        try {
            Method snapshotMethod = service.getClass().getMethod('snapshot')
            raw = snapshotMethod.invoke(service)
        } catch (NoSuchMethodException e) {
            fail('durexDependencyRegistry does not expose the v2 snapshot() contract', e)
            return null
        } catch (InvocationTargetException e) {
            Throwable cause = e.cause ?: e
            throw new GradleException(
                    "Durex bootstrap error\nProblem: dependency snapshot export failed\nCause: ${cause.message}",
                    cause)
        } catch (ReflectiveOperationException e) {
            fail("cannot invoke dependency snapshot(): ${e.message}", e)
            return null
        }

        if (!(raw instanceof Map)) {
            fail("dependency snapshot() returned ${raw == null ? 'null' : raw.getClass().name}, expected java.util.Map")
        }
        fromSnapshot(raw as Map)
    }

    static DependencyCatalogSnapshot fromSnapshot(Map raw) {
        Object schema = raw.get('schemaVersion')
        if (!(schema instanceof Number)) {
            fail('dependency snapshot schemaVersion must be an integer')
        }
        int actualSchema = (schema as Number).intValue()
        if (actualSchema != EXPECTED_SCHEMA_VERSION) {
            throw new GradleException(
                    'Durex bootstrap error\n' +
                    'Problem: unsupported dependency snapshot schema\n' +
                    "Expected: ${EXPECTED_SCHEMA_VERSION}\n" +
                    "Actual: ${actualSchema}")
        }

        Object javaNode = raw.get('javaVersion')
        if (!(javaNode instanceof Number)) {
            fail('dependency snapshot javaVersion must be an integer')
        }

        Map platformsRaw = table(raw, 'platforms')
        Map librariesRaw = table(raw, 'libraries')
        Map pluginsRaw = table(raw, 'plugins')

        Map<String, CatalogPlatform> platforms = new LinkedHashMap<>()
        platformsRaw.each { alias, node ->
            Map entry = entry(node, "platform '${alias}'")
            platforms.put(alias as String, new CatalogPlatform(
                    alias as String,
                    requiredString(entry, 'module', "platform '${alias}'"),
                    requiredString(entry, 'version', "platform '${alias}'")))
        }

        Map<String, CatalogLibrary> libraries = new LinkedHashMap<>()
        librariesRaw.each { alias, node ->
            Map entry = entry(node, "library '${alias}'")
            libraries.put(alias as String, new CatalogLibrary(
                    alias as String,
                    requiredString(entry, 'module', "library '${alias}'"),
                    optionalString(entry, 'version', "library '${alias}'"),
                    optionalString(entry, 'platform', "library '${alias}'")))
        }

        Map<String, CatalogPlugin> plugins = new LinkedHashMap<>()
        pluginsRaw.each { alias, node ->
            Map entry = entry(node, "plugin '${alias}'")
            plugins.put(alias as String, new CatalogPlugin(
                    alias as String,
                    requiredString(entry, 'id', "plugin '${alias}'"),
                    optionalString(entry, 'module', "plugin '${alias}'"),
                    requiredString(entry, 'version', "plugin '${alias}'")))
        }

        new DependencyCatalogSnapshot((javaNode as Number).intValue(), platforms, libraries, plugins)
    }

    private static Map table(Map raw, String key) {
        Object value = raw.get(key)
        if (!(value instanceof Map)) {
            fail("dependency snapshot '${key}' must be a map")
        }
        value as Map
    }

    private static Map entry(Object value, String subject) {
        if (!(value instanceof Map)) {
            fail("dependency snapshot ${subject} must be a map")
        }
        value as Map
    }

    private static String requiredString(Map entry, String key, String subject) {
        Object value = entry.get(key)
        if (!(value instanceof String) || (value as String).trim().isEmpty()) {
            fail("dependency snapshot ${subject}.${key} must be a non-empty string")
        }
        value as String
    }

    private static String optionalString(Map entry, String key, String subject) {
        Object value = entry.get(key)
        if (value == null) return null
        if (!(value instanceof String) || (value as String).trim().isEmpty()) {
            fail("dependency snapshot ${subject}.${key} must be a non-empty string when present")
        }
        value as String
    }

    private static void fail(String problem, Throwable cause = null) {
        String message = "Durex bootstrap error\nProblem: ${problem}"
        if (cause == null) {
            throw new GradleException(message)
        }
        throw new GradleException(message, cause)
    }
}
