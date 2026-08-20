package com.github.durex.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class DurexModulePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        DurexModuleState state = new DurexModuleState()
        project.extensions.add(DurexModuleState, 'durexModuleState', state)
        project.extensions.create('durex', DurexExtension, project, state)
    }
}
