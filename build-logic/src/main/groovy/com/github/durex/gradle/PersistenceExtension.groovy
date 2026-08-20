package com.github.durex.gradle

import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.Project

class PersistenceExtension {
    protected final Project project
    protected final DurexModuleModel model

    PersistenceExtension(Project project, DurexModuleModel model) {
        this.project = project
        this.model = model
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
