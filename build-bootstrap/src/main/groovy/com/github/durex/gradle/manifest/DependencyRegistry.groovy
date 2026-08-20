package com.github.durex.gradle.manifest

import org.gradle.api.GradleException

final class DependencyRegistry {
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

    private static <T> T required(Map<String, T> values, String kind, String id) {
        T value = values.get(id)
        if (value == null) {
            throw new GradleException("Durex dependency manifest error\nProblem: unknown ${kind} '${id}'")
        }
        value
    }
}
