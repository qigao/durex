package com.github.durex.gradle.manifest

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class DependencyRegistryService implements BuildService<DependencyRegistryService.Parameters>, AutoCloseable {
    interface Parameters extends BuildServiceParameters {
        RegularFileProperty getManifestFile()
    }

    private volatile DependencyRegistry registry

    private DependencyRegistry registry() {
        DependencyRegistry current = registry
        if (current == null) {
            synchronized (this) {
                current = registry
                if (current == null) {
                    current = DependencyManifestLoader.load(parameters.manifestFile.get().asFile)
                    registry = current
                }
            }
        }
        current
    }

    int javaVersion() { registry().javaVersion() }
    VersionSpec version(String id) { registry().version(id) }
    PlatformSpec platform(String id) { registry().platform(id) }
    LibrarySpec library(String id) { registry().library(id) }
    PluginSpec plugin(String alias) { registry().plugin(alias) }
    PluginSpec pluginByGradleId(String id) { registry().pluginByGradleId(id) }
    Collection<PluginSpec> plugins() { registry().plugins() }

    @Override
    void close() {}
}
