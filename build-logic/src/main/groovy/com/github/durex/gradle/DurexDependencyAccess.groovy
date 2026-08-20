package com.github.durex.gradle

import com.github.durex.gradle.catalog.DependencyCatalogSnapshot
import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.GradleException
import org.gradle.api.Project

final class DurexDependencyAccess {
    static int javaVersion(Project project) {
        catalog(project).javaVersion()
    }

    static void activatePlatform(Project project, DurexModuleModel model, String configuration, String platformAlias) {
        if (project.configurations.findByName(configuration) == null) return
        String binding = "${configuration}:${platformAlias}"
        if (!model.platformBindings.get().contains(binding)) {
            def platform = catalog(project).platform(platformAlias)
            project.dependencies.add(configuration, project.dependencies.platform(platform.coordinate()))
            model.bindPlatform(configuration, platformAlias)
        }
    }

    static void add(Project project, DurexModuleModel model, String configuration, String alias) {
        def library = catalog(project).library(alias)
        if (library.platform) {
            activatePlatform(project, model, configuration, library.platform as String)
        }
        project.dependencies.add(configuration, library.notation())
    }

    static String libraryNotation(Project project, DurexModuleModel model, String alias) {
        def library = catalog(project).library(alias)
        if (library.platform && !model.platformBindings.get().any { it.endsWith(":${library.platform}") }) {
            throw new GradleException(
                    "Durex dependency error\nLibrary: ${alias}\nProblem: platform '${library.platform}' is not active for this project")
        }
        library.notation() as String
    }

    static DependencyCatalogSnapshot catalog(Project project) {
        DependencyCatalogSnapshot catalog = project.extensions.findByType(DependencyCatalogSnapshot)
        if (catalog == null) {
            throw new GradleException(
                    'Durex dependency catalog error\nProblem: durexDependencyCatalog is not available; apply durex.catalog')
        }
        catalog
    }
}
