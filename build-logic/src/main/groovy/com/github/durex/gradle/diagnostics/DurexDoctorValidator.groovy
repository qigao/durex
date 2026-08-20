package com.github.durex.gradle.diagnostics

import com.github.durex.gradle.capability.CapabilityPluginRegistry
import com.github.durex.gradle.capability.CapabilityRegistry
import com.github.durex.gradle.capability.CapabilitySpec
import com.github.durex.gradle.catalog.CatalogLibrary
import com.github.durex.gradle.catalog.DependencyCatalogSnapshot
import com.github.durex.gradle.model.DurexModuleModel

final class DurexDoctorValidator {
    static List<String> validate(
            String projectPath,
            DurexModuleModel model,
            CapabilityRegistry registry,
            CapabilityPluginRegistry pluginRegistry,
            DependencyCatalogSnapshot catalog) {
        Set<String> violations = new TreeSet<>()
        Set<String> enabled = new TreeSet<>(model.capabilities.get())
        Set<String> bindings = new TreeSet<>(model.platformBindings.get())

        if (!model.moduleKind.isPresent()) {
            violations.add('module type is not selected')
        }

        pluginRegistry.all().each { pluginId, capabilityId ->
            if (!registry.contains(capabilityId)) {
                violations.add("plugin '${pluginId}' maps to unregistered capability '${capabilityId}'")
            }
        }

        enabled.each { capabilityId ->
            CapabilitySpec spec = registry.get(capabilityId)
            if (spec == null) {
                violations.add("enabled capability '${capabilityId}' is not registered")
                return
            }

            if (!spec.allowedModules.isEmpty()) {
                if (!model.moduleKind.isPresent() || !spec.allowedModules.contains(model.moduleKind.get())) {
                    violations.add("capability '${capabilityId}' is not allowed for module type ${model.moduleKind.isPresent() ? model.moduleKind.get() : 'NONE'}")
                }
            }

            spec.requires.each { required ->
                if (!enabled.contains(required)) {
                    violations.add("capability '${capabilityId}' requires '${required}'")
                }
            }

            spec.conflicts.each { conflict ->
                if (enabled.contains(conflict)) {
                    violations.add("capability '${capabilityId}' conflicts with '${conflict}'")
                }
            }

            spec.dependencies.each { dependency ->
                CatalogLibrary library = catalog.libraries().find { it.alias == dependency.libraryAlias }
                if (library == null) {
                    violations.add("capability '${capabilityId}' references unknown library '${dependency.libraryAlias}'")
                } else if (library.platform != null) {
                    String expectedBinding = "${dependency.configuration}:${library.platform}"
                    if (!bindings.contains(expectedBinding)) {
                        violations.add("capability '${capabilityId}' requires platform binding '${expectedBinding}'")
                    }
                }
            }

            spec.externalPluginAliases.each { alias ->
                if (!catalog.plugins().any { it.alias == alias }) {
                    violations.add("capability '${capabilityId}' references unknown plugin alias '${alias}'")
                }
            }
        }

        Collections.unmodifiableList(new ArrayList<>(violations))
    }

    private DurexDoctorValidator() {}
}
