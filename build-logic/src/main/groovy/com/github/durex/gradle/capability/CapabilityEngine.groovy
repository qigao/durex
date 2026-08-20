package com.github.durex.gradle.capability

import com.github.durex.gradle.DurexConfigurationException
import com.github.durex.gradle.catalog.DependencyCatalogSnapshot
import com.github.durex.gradle.dependency.DependencyBridge
import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.Project

final class CapabilityEngine {
    private final Project project
    private final DurexModuleModel model
    private final DependencyCatalogSnapshot catalog
    private final CapabilityRegistry registry
    private final CapabilityPluginRegistry pluginRegistry
    private final Deque<String> activationStack = new ArrayDeque<>()

    CapabilityEngine(
            Project project,
            DurexModuleModel model,
            DependencyCatalogSnapshot catalog,
            CapabilityRegistry registry,
            CapabilityPluginRegistry pluginRegistry) {
        this.project = project
        this.model = model
        this.catalog = catalog
        this.registry = registry
        this.pluginRegistry = pluginRegistry
    }

    synchronized void enable(String capabilityId) {
        if (model.capabilities.get().contains(capabilityId)) {
            return
        }

        CapabilitySpec spec = registry.get(capabilityId)
        if (spec == null) {
            fail(capabilityId, "capability '${capabilityId}' is not registered")
        }

        if (activationStack.contains(capabilityId)) {
            List<String> cycle = new ArrayList<>(activationStack)
            int start = cycle.indexOf(capabilityId)
            cycle = cycle.subList(start, cycle.size())
            cycle.add(capabilityId)
            fail(capabilityId, "capability requirement cycle detected: ${cycle.join(' -> ')}")
        }

        validateModule(spec)
        validateConflicts(spec)

        activationStack.addLast(capabilityId)
        try {
            spec.requires.each { requiredId ->
                if (!registry.contains(requiredId)) {
                    fail(capabilityId, "required capability '${requiredId}' is not registered")
                }
                enable(requiredId)
            }

            validateConflicts(spec)

            spec.externalPluginAliases.each { pluginAlias ->
                def plugin = catalog.plugin(pluginAlias)
                project.pluginManager.apply(plugin.id)
            }

            spec.dependencies.each { binding ->
                DependencyBridge.add(project, model, binding.configuration, binding.libraryAlias)
            }

            model.enableCapability(capabilityId)
        } finally {
            activationStack.removeLastOccurrence(capabilityId)
        }
    }

    private void validateModule(CapabilitySpec spec) {
        if (spec.allowedModules.isEmpty()) return
        if (!model.moduleKind.isPresent()) {
            fail(spec.id,
                    "capability '${spec.id}' requires a module type; allowed: ${spec.allowedModules.join(',')}")
        }
        def current = model.moduleKind.get()
        if (!spec.allowedModules.contains(current)) {
            throw new DurexConfigurationException(
                    'Durex configuration error\n' +
                    "Project: ${project.path}\n" +
                    "Capability: ${spec.id}\n" +
                    "Module type: ${current}\n" +
                    "Problem: capability '${spec.id}' is not supported by ${current}\n" +
                    "Allowed module types: ${spec.allowedModules.join(',')}")
        }
    }

    private void validateConflicts(CapabilitySpec requested) {
        Set<String> active = model.capabilities.get()
        requested.conflicts.each { conflictId ->
            if (active.contains(conflictId)) {
                conflict(requested.id, conflictId)
            }
        }
        active.each { activeId ->
            CapabilitySpec activeSpec = registry.get(activeId)
            if (activeSpec != null && activeSpec.conflicts.contains(requested.id)) {
                conflict(requested.id, activeId)
            }
        }
    }

    private void conflict(String requested, String existing) {
        throw new DurexConfigurationException(
                'Durex configuration error\n' +
                "Project: ${project.path}\n" +
                "Capability: ${requested}\n" +
                "Problem: conflicts with enabled capability '${existing}'")
    }

    private void fail(String capabilityId, String problem) {
        throw new DurexConfigurationException(
                'Durex configuration error\n' +
                "Project: ${project.path}\n" +
                "Capability: ${capabilityId}\n" +
                "Problem: ${problem}")
    }
}
