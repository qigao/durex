package com.github.durex.gradle

import com.github.durex.gradle.capability.CapabilityEngine
import com.github.durex.gradle.capability.CapabilityPluginRegistry
import com.github.durex.gradle.dependency.DependencyBridge
import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.UnknownPluginException

class DurexExtension {
    private final Project project
    private final DurexModuleModel model
    private final PersistenceExtension persistence

    DurexExtension(Project project, DurexModuleModel model) {
        this.project = project
        this.model = model
        this.persistence = new PersistenceExtension(project, model)
    }

    String library(String alias) {
        DependencyBridge.explicitNotation(project, alias)
    }

    void dependency(String configuration, String alias) {
        DependencyBridge.add(project, model, configuration, alias)
    }

    void capability(String pluginId) {
        try {
            project.pluginManager.apply(pluginId)
        } catch (UnknownPluginException e) {
            throw new DurexConfigurationException(
                    'Durex configuration error\n' +
                    "Project: ${project.path}\n" +
                    "Gradle plugin id: ${pluginId}\n" +
                    'Problem: capability plugin could not be resolved',
                    e)
        }

        CapabilityPluginRegistry pluginRegistry = project.extensions.getByType(CapabilityPluginRegistry)
        String capabilityId = pluginRegistry.capabilityForPlugin(pluginId)
        if (capabilityId == null) {
            throw new DurexConfigurationException(
                    'Durex configuration error\n' +
                    "Project: ${project.path}\n" +
                    "Gradle plugin id: ${pluginId}\n" +
                    'Problem: plugin did not register a Durex primary capability')
        }
        project.extensions.getByType(CapabilityEngine).enable(capabilityId)
    }

    PersistenceExtension getPersistence() {
        persistence
    }

    void persistence(Action<? super PersistenceExtension> action) {
        action.execute(persistence)
    }

    void persistence(Closure closure) {
        Closure configured = closure.rehydrate(persistence, closure.owner, closure.thisObject)
        configured.resolveStrategy = Closure.DELEGATE_FIRST
        configured.call()
    }

    void aop() {
        project.pluginManager.apply('durex.feature.aop')
    }

    void transaction() {
        project.pluginManager.apply('durex.feature.transaction')
    }

    void redis() {
        project.pluginManager.apply('durex.feature.redis')
    }

    void nativeImage() {
        project.pluginManager.apply('durex.feature.native')
    }

    void lombok() {
        project.pluginManager.apply('durex.feature.lombok')
    }
}
