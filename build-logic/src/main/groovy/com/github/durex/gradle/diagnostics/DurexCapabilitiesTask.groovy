package com.github.durex.gradle.diagnostics

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class DurexCapabilitiesTask extends DefaultTask {
    @Input abstract Property<String> getModuleKind()
    @Input abstract Property<Integer> getJavaVersion()
    @Input abstract ListProperty<String> getCapabilities()
    @Input abstract ListProperty<String> getPlatformBindings()

    @TaskAction
    void report() {
        List<String> capabilityValues = capabilities.get()
        List<String> bindings = platformBindings.get()
        Set<String> platforms = bindings.collect { binding ->
            binding.substring(binding.indexOf(':') + 1)
        } as TreeSet<String>

        println "Type: ${moduleKind.get()}"
        println "Java: ${javaVersion.get()}"
        println "Platforms: ${platforms.join(',')}"
        println "Platform bindings: ${bindings.join(',')}"
        println "Features: ${capabilityValues.join(',')}"
        println "Native: ${capabilityValues.contains('native') ? 'enabled' : 'disabled'}"
    }
}
