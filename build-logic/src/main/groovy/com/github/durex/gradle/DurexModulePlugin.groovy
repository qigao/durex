package com.github.durex.gradle

import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.Plugin
import org.gradle.api.Project

class DurexModulePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('durex.catalog')

        DurexModuleModel model = project.extensions.create('durexModuleModel', DurexModuleModel)
        model.capabilities.convention(Collections.emptySet())
        model.platformBindings.convention(Collections.emptySet())

        project.extensions.create('durex', DurexExtension, project, model)

        project.tasks.register('durexCapabilities') {
            group = 'Durex'
            description = 'Print Durex module type and active capabilities.'
            doLast {
                Set<String> capabilities = model.capabilities.get()
                Set<String> bindings = model.platformBindings.get()
                Set<String> platforms = bindings.collect { it.substring(it.indexOf(':') + 1) } as TreeSet<String>
                println "Type: ${model.moduleKind.isPresent() ? model.moduleKind.get() : 'NONE'}"
                println "Java: ${DurexDependencyAccess.javaVersion(project)}"
                println "Platforms: ${platforms.join(',')}"
                println "Features: ${new TreeSet<>(capabilities).join(',')}"
                println "Native: ${capabilities.contains('native') ? 'enabled' : 'disabled'}"
            }
        }
    }
}
