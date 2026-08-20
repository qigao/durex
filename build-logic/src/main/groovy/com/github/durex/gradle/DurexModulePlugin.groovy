package com.github.durex.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class DurexModulePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('durex.catalog')

        DurexModuleState state = new DurexModuleState()
        project.extensions.add(DurexModuleState, 'durexModuleState', state)
        project.extensions.create('durex', DurexExtension, project, state)

        project.tasks.register('durexCapabilities') {
            group = 'Durex'
            description = 'Print Durex module type and active capabilities.'
            doLast {
                println "Type: ${state.kind() ?: 'NONE'}"
                println "Java: ${DurexDependencyAccess.javaVersion(project)}"
                println "Platforms: ${state.activePlatforms().join(',')}"
                println "Features: ${state.activeFeatures().join(',')}"
                println "Native: ${state.nativeEnabled() ? 'enabled' : 'disabled'}"
            }
        }
    }
}
