package com.github.durex.gradle.model

import com.github.durex.gradle.ModuleKind
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

abstract class DurexModuleModel {
    abstract Property<ModuleKind> getModuleKind()
    abstract SetProperty<String> getCapabilities()
    abstract SetProperty<String> getPlatformBindings()

    void claim(ModuleKind requested, String projectPath) {
        if (!moduleKind.isPresent()) {
            moduleKind.set(requested)
            return
        }
        ModuleKind existing = moduleKind.get()
        if (existing != requested) {
            throw new com.github.durex.gradle.DurexConfigurationException(
                    'Durex configuration error\n' +
                    "Project: ${projectPath}\n" +
                    'Problem: module type conflict\n' +
                    "Existing: ${existing}\n" +
                    "Requested: ${requested}")
        }
    }

    void requireFeature(String featurePlugin, String projectPath, ModuleKind... allowed) {
        ModuleKind current = moduleKind.isPresent() ? moduleKind.get() : null
        if (current != null && allowed.contains(current)) return
        String requirement = allowed.collect { kindPluginId(it) }.join(' or ')
        throw new GradleException("${featurePlugin} requires ${requirement}")
    }

    void enableCapability(String capability) {
        capabilities.add(capability)
    }

    void bindPlatform(String configuration, String platformAlias) {
        platformBindings.add("${configuration}:${platformAlias}" as String)
    }

    private static String kindPluginId(ModuleKind kind) {
        switch (kind) {
            case ModuleKind.JAVA_LIBRARY: return 'durex.java-library'
            case ModuleKind.SPRING_LIBRARY: return 'durex.spring-library'
            case ModuleKind.SPRING_SERVICE: return 'durex.spring-service'
            default: return kind.name()
        }
    }
}
