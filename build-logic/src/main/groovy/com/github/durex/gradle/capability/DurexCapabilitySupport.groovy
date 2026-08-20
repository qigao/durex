package com.github.durex.gradle.capability

import org.gradle.api.Project

final class DurexCapabilitySupport {
    static void registerAndEnable(Project project, String pluginId, CapabilitySpec spec) {
        CapabilityRegistry registry = project.extensions.getByType(CapabilityRegistry)
        CapabilityPluginRegistry pluginRegistry = project.extensions.getByType(CapabilityPluginRegistry)
        CapabilityEngine engine = project.extensions.getByType(CapabilityEngine)

        registry.register(spec)
        pluginRegistry.register(pluginId, spec.id)
        engine.enable(spec.id)
    }

    private DurexCapabilitySupport() {}
}
