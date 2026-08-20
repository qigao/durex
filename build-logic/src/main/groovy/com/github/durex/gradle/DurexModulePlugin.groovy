package com.github.durex.gradle

import com.github.durex.gradle.capability.BuiltinCapabilities
import com.github.durex.gradle.capability.CapabilityEngine
import com.github.durex.gradle.capability.CapabilityPluginRegistry
import com.github.durex.gradle.capability.CapabilityRegistry
import com.github.durex.gradle.catalog.DependencyCatalogSnapshot
import com.github.durex.gradle.diagnostics.DurexCapabilitiesTask
import com.github.durex.gradle.diagnostics.DurexDoctorTask
import com.github.durex.gradle.diagnostics.DurexDoctorValidator
import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.Plugin
import org.gradle.api.Project

class DurexModulePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('durex.catalog')

        DependencyCatalogSnapshot catalog = project.extensions.getByType(DependencyCatalogSnapshot)
        DurexModuleModel model = project.extensions.create('durexModuleModel', DurexModuleModel)
        model.capabilities.convention(Collections.emptySet())
        model.platformBindings.convention(Collections.emptySet())

        CapabilityRegistry capabilityRegistry = new CapabilityRegistry()
        CapabilityPluginRegistry pluginRegistry = new CapabilityPluginRegistry()
        CapabilityEngine capabilityEngine = new CapabilityEngine(
                project, model, catalog, capabilityRegistry, pluginRegistry)
        project.extensions.add(CapabilityRegistry, 'durexCapabilityRegistry', capabilityRegistry)
        project.extensions.add(CapabilityPluginRegistry, 'durexCapabilityPluginRegistry', pluginRegistry)
        project.extensions.add(CapabilityEngine, 'durexCapabilityEngine', capabilityEngine)
        BuiltinCapabilities.registerAll(capabilityRegistry)

        project.extensions.create('durex', DurexExtension, project, model)

        def sortedCapabilities = model.capabilities.map { values ->
            new ArrayList<String>(new TreeSet<String>(values))
        }
        def sortedBindings = model.platformBindings.map { values ->
            new ArrayList<String>(new TreeSet<String>(values))
        }
        def moduleKindName = model.moduleKind.map { it.name() }.orElse('NONE')

        project.tasks.register('durexCapabilities', DurexCapabilitiesTask) { task ->
            task.group = 'Durex'
            task.description = 'Print Durex module type and active capabilities.'
            task.moduleKind.set(moduleKindName)
            task.javaVersion.set(catalog.javaVersion())
            task.capabilities.set(sortedCapabilities)
            task.platformBindings.set(sortedBindings)
        }

        def doctor = project.tasks.register('durexDoctor', DurexDoctorTask) { task ->
            task.group = 'Durex'
            task.description = 'Validate Durex module configuration consistency.'
            task.projectPathInput.set(project.path)
            task.moduleKind.set(moduleKindName)
            task.capabilities.set(sortedCapabilities)
            task.platformBindings.set(sortedBindings)
            task.violations.convention(Collections.emptyList())
        }

        project.afterEvaluate {
            List<String> violations = DurexDoctorValidator.validate(
                    project.path, model, capabilityRegistry, pluginRegistry, catalog)
            doctor.configure { task ->
                task.violations.set(violations)
            }
        }
    }
}
