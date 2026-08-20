package com.github.durex.gradle.settings

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class DurexProjectsTask extends DefaultTask {
    @Input abstract ListProperty<String> getProjectLines()

    @TaskAction
    void report() {
        println 'Durex Projects'
        projectLines.get().each { println "  ${it}" }
    }
}
