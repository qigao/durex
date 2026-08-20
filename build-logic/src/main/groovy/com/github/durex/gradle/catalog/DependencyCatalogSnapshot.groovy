package com.github.durex.gradle.catalog

import org.gradle.api.GradleException

final class DependencyCatalogSnapshot {
    private final int javaVersion
    private final Map<String, CatalogPlatform> platforms
    private final Map<String, CatalogLibrary> libraries
    private final Map<String, CatalogPlugin> plugins
    private final Map<String, CatalogPlugin> pluginsByGradleId

    DependencyCatalogSnapshot(
            int javaVersion,
            Map<String, CatalogPlatform> platforms,
            Map<String, CatalogLibrary> libraries,
            Map<String, CatalogPlugin> plugins) {
        this.javaVersion = javaVersion
        this.platforms = Collections.unmodifiableMap(new LinkedHashMap<>(platforms))
        this.libraries = Collections.unmodifiableMap(new LinkedHashMap<>(libraries))
        this.plugins = Collections.unmodifiableMap(new LinkedHashMap<>(plugins))

        Map<String, CatalogPlugin> byGradleId = new LinkedHashMap<>()
        plugins.values().each { plugin ->
            CatalogPlugin previous = byGradleId.put(plugin.id, plugin)
            if (previous != null) {
                throw new GradleException(
                        "Durex dependency catalog error\nProblem: duplicate Gradle plugin id '${plugin.id}'")
            }
        }
        this.pluginsByGradleId = Collections.unmodifiableMap(byGradleId)
    }

    int javaVersion() { javaVersion }

    CatalogPlatform platform(String alias) {
        required(platforms, 'platform', alias)
    }

    CatalogLibrary library(String alias) {
        required(libraries, 'library', alias)
    }

    CatalogPlugin plugin(String alias) {
        required(plugins, 'plugin', alias)
    }

    CatalogPlugin pluginByGradleId(String id) {
        pluginsByGradleId.get(id)
    }

    Collection<CatalogPlatform> platforms() {
        Collections.unmodifiableList(new ArrayList<>(platforms.values()))
    }

    Collection<CatalogLibrary> libraries() {
        Collections.unmodifiableList(new ArrayList<>(libraries.values()))
    }

    Collection<CatalogPlugin> plugins() {
        Collections.unmodifiableList(new ArrayList<>(plugins.values()))
    }

    private static <T> T required(Map<String, T> values, String kind, String alias) {
        T value = values.get(alias)
        if (value == null) {
            throw new GradleException(
                    "Durex dependency catalog error\n${kind.capitalize()}: ${alias}\nProblem: unknown ${kind} alias")
        }
        value
    }
}
