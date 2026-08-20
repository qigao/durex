package com.github.durex.gradle

import com.github.durex.gradle.model.DurexModuleModel
import org.gradle.api.Action
import org.gradle.api.Project

class DurexExtension {
    private final Project project
    private final DurexModuleModel model
    private final PersistenceExtension persistence

    DurexExtension(Project project, DurexModuleModel model) {
        this.project = project
        this.model = model
        this.persistence = new PersistenceExtension(project, model)
    }

    String library(String alias) {
        DurexDependencyAccess.libraryNotation(project, model, alias)
    }

    PersistenceExtension getPersistence() {
        persistence
    }

    void persistence(Action<? super PersistenceExtension> action) {
        action.execute(persistence)
    }

    void persistence(Closure closure) {
        Closure configured = closure.rehydrate(persistence, closure.owner, closure.thisObject)
        configured.resolveStrategy = Closure.DELEGATE_FIRST
        configured.call()
    }

    void redis() {
        project.pluginManager.apply('durex.feature.redis')
    }

    void nativeImage() {
        project.pluginManager.apply('durex.feature.native')
    }

    void lombok() {
        project.pluginManager.apply('durex.feature.lombok')
    }
}
