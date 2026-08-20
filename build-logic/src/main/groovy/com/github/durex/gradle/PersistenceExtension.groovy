package com.github.durex.gradle

import org.gradle.api.Project

class PersistenceExtension {
    protected final Project project
    protected final DurexModuleState state

    PersistenceExtension(Project project, DurexModuleState state) {
        this.project = project
        this.state = state
    }

    void jpa() {
        project.pluginManager.apply('durex.feature.jpa')
    }

    void jdbc() {
        project.pluginManager.apply('durex.feature.jdbc')
    }

    void jooq() {
        project.pluginManager.apply('durex.feature.jooq')
    }
}
