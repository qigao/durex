package com.github.durex.gradle.diagnostics

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class DurexDoctorTask extends DefaultTask {
    @Input abstract Property<String> getProjectPathInput()
    @Input abstract Property<String> getModuleKind()
    @Input abstract ListProperty<String> getCapabilities()
    @Input abstract ListProperty<String> getPlatformBindings()
    @Input abstract ListProperty<String> getViolations()

    @TaskAction
    void diagnose() {
        println "Durex Doctor — ${projectPathInput.get()}"
        println "Module: ${moduleKind.get()}"
        println "Capabilities: ${capabilities.get().join(',')}"
        println "Platform bindings: ${platformBindings.get().join(',')}"

        List<String> problems = violations.get()
        if (problems.isEmpty()) {
            println 'Manifest: OK'
            println 'Configuration: OK'
            return
        }

        String details = problems.collect { "  - ${it}" }.join('\n')
        throw new GradleException(
                "Durex Doctor — ${projectPathInput.get()}\nConfiguration: FAILED\n${details}")
    }
}
