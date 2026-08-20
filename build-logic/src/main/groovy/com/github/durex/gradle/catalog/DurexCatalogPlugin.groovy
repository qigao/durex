package com.github.durex.gradle.catalog

import org.gradle.api.Plugin
import org.gradle.api.Project

class DurexCatalogPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (project.extensions.findByName('durexDependencyCatalog') != null) {
            return
        }
        DependencyCatalogSnapshot catalog = DurexRegistryBridge.fromProject(project)
        project.extensions.add(DependencyCatalogSnapshot, 'durexDependencyCatalog', catalog)
    }
}
