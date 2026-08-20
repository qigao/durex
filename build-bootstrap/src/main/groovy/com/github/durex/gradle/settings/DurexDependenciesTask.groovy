package com.github.durex.gradle.settings

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class DurexDependenciesTask extends DefaultTask {
    @Input abstract Property<Integer> getJavaVersion()
    @Input abstract ListProperty<String> getPlatformLines()
    @Input abstract ListProperty<String> getPluginLines()
    @Input abstract ListProperty<String> getLibraryLines()

    @TaskAction
    void report() {
        println 'Durex Dependencies'
        println 'Java'
        println "  ${javaVersion.get()}"
        println 'Platforms'
        platformLines.get().each { println "  ${it}" }
        println 'Plugins'
        pluginLines.get().each { println "  ${it}" }
        println 'Libraries'
        libraryLines.get().each { println "  ${it}" }
    }
}
