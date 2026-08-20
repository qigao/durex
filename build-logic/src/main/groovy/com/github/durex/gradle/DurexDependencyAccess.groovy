package com.github.durex.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project

final class DurexDependencyAccess {
    static int javaVersion(Project project) {
        service(project).javaVersion() as int
    }

    static void activatePlatform(Project project, DurexModuleState state, String configuration, String platformAlias) {
        if (project.configurations.findByName(configuration) == null) return
        if (state.activatePlatform(configuration, platformAlias)) {
            def platform = service(project).platform(platformAlias)
            project.dependencies.add(configuration, project.dependencies.platform(platform.coordinate()))
        }
    }

    static void add(Project project, DurexModuleState state, String configuration, String alias) {
        def library = service(project).library(alias)
        if (library.platform) {
            activatePlatform(project, state, configuration, library.platform as String)
        }
        project.dependencies.add(configuration, library.notation())
    }

    static String libraryNotation(Project project, DurexModuleState state, String alias) {
        def library = service(project).library(alias)
        if (library.platform && !state.activePlatforms().contains(library.platform as String)) {
            throw new GradleException(
                    "Durex dependency error\nLibrary: ${alias}\nProblem: platform '${library.platform}' is not active for this project")
        }
        library.notation() as String
    }

    static Object service(Project project) {
        def registration = project.gradle.sharedServices.registrations.findByName('durexDependencyRegistry')
        if (registration == null) {
            throw new GradleException(
                    'Durex dependency error\nProblem: durexDependencyRegistry is not available; apply durex.settings in this build')
        }
        registration.service.get()
    }
}
