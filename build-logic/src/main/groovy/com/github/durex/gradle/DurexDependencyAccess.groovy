package com.github.durex.gradle

import com.github.durex.gradle.catalog.DependencyCatalogSnapshot
import com.github.durex.gradle.dependency.DependencyBridge
import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.GradleException
import org.gradle.api.Project

final class DurexDependencyAccess {
    static int javaVersion(Project project) {
        catalog(project).javaVersion()
    }

    static void activatePlatform(Project project, DurexModuleModel model, String configuration, String platformAlias) {
        DependencyBridge.activatePlatform(project, model, configuration, platformAlias)
    }

    static void add(Project project, DurexModuleModel model, String configuration, String alias) {
        DependencyBridge.add(project, model, configuration, alias)
    }

    static String libraryNotation(Project project, DurexModuleModel model, String alias) {
        DependencyBridge.explicitNotation(project, alias)
    }

    static DependencyCatalogSnapshot catalog(Project project) {
        DependencyCatalogSnapshot catalog = project.extensions.findByType(DependencyCatalogSnapshot)
        if (catalog == null) {
            throw new GradleException(
                    'Durex dependency catalog error\nProblem: durexDependencyCatalog is not available; apply durex.module')
        }
        catalog
    }
}
