package com.github.durex.gradle

import org.gradle.api.Project

class PersistenceExtension {
    protected final Project project
    protected final DurexModuleState state

    PersistenceExtension(Project project, DurexModuleState state) {
        this.project = project
        this.state = state
    }
}
