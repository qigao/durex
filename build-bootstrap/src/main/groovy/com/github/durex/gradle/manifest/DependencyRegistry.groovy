package com.github.durex.gradle.manifest

import org.gradle.api.GradleException

final class DependencyRegistry {
    static final int SNAPSHOT_SCHEMA_VERSION = 1

    private final int javaVersion
    private final Map<String, VersionSpec> versions
    private final Map<String, PlatformSpec> platforms
    private final Map<String, LibrarySpec> libraries
    private final Map<String, PluginSpec> plugins
    private final Map<String, PluginSpec> pluginsByGradleId

    DependencyRegistry(
            int javaVersion,
            Map<String, VersionSpec> versions,
            Map<String, PlatformSpec> platforms,
            Map<String, LibrarySpec> libraries,
            Map<String, PluginSpec> plugins,
            Map<String, PluginSpec> pluginsByGradleId) {
        this.javaVersion = javaVersion
        this.versions = Collections.unmodifiableMap(new LinkedHashMap<>(versions))
        this.platforms = Collections.unmodifiableMap(new LinkedHashMap<>(platforms))
        this.libraries = Collections.unmodifiableMap(new LinkedHashMap<>(libraries))
        this.plugins = Collections.unmodifiableMap(new LinkedHashMap<>(plugins))
        this.pluginsByGradleId = Collections.unmodifiableMap(new LinkedHashMap<>(pluginsByGradleId))
    }

    int javaVersion() { javaVersion }

    VersionSpec version(String id) {
        required(versions, 'version', id)
    }

    PlatformSpec platform(String id) {
        required(platforms, 'platform', id)
    }

    LibrarySpec library(String id) {
        required(libraries, 'library', id)
    }

    PluginSpec plugin(String alias) {
        required(plugins, 'plugin', alias)
    }

    PluginSpec pluginByGradleId(String id) {
        pluginsByGradleId.get(id)
    }

    Collection<PluginSpec> plugins() {
        plugins.values()
    }

    Map<String, Object> snapshot() {
        deepFreeze([
                schemaVersion: SNAPSHOT_SCHEMA_VERSION,
                javaVersion: javaVersion,
                platforms: platforms.collectEntries { alias, platform ->
                    [(alias): [
                            module: platform.module,
                            version: platform.version
                    ]]
                },
                libraries: libraries.collectEntries { alias, library ->
                    Map<String, Object> entry = [module: library.module]
                    if (library.version != null) entry.version = library.version
                    if (library.platform != null) entry.platform = library.platform
                    [(alias): entry]
                },
                plugins: plugins.collectEntries { alias, plugin ->
                    Map<String, Object> entry = [
                            id: plugin.id,
                            version: plugin.version
                    ]
                    if (plugin.module != null) entry.module = plugin.module
                    [(alias): entry]
                }
        ]) as Map<String, Object>
    }

    private static Object deepFreeze(Object value) {
        if (value instanceof Map) {
            Map<Object, Object> copy = new LinkedHashMap<>()
            (value as Map).each { key, nested ->
                if (!(key instanceof String)) {
                    throw new GradleException(
                            "Durex bootstrap error\nProblem: dependency snapshot map key must be a String")
                }
                copy.put(key, deepFreeze(nested))
            }
            return Collections.unmodifiableMap(copy)
        }
        if (value instanceof List) {
            List<Object> copy = (value as List).collect { nested -> deepFreeze(nested) }
            return Collections.unmodifiableList(copy)
        }
        if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
            return value
        }
        throw new GradleException(
                "Durex bootstrap error\nProblem: unsupported dependency snapshot value type '${value == null ? 'null' : value.getClass().name}'")
    }

    private static <T> T required(Map<String, T> values, String kind, String id) {
        T value = values.get(id)
        if (value == null) {
            throw new GradleException("Durex dependency manifest error\nProblem: unknown ${kind} '${id}'")
        }
        value
    }
}
