package com.github.durex.gradle.settings

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class DurexBuildLogicPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def registration = project.gradle.sharedServices.registrations.findByName('durexDependencyRegistry')
        if (registration == null) {
            throw new GradleException(
                    'Durex build-logic bootstrap error\nProblem: durexDependencyRegistry was not registered during Durex build-logic settings bootstrap')
        }
        def registry = registration.service.get()

        project.dependencies.add('implementation', registry.plugin('spring-boot').coordinate())
        project.dependencies.add('implementation', registry.plugin('graalvm-native').coordinate())
        project.dependencies.add('implementation', registry.plugin('jooq-codegen').coordinate())
        project.dependencies.add('implementation', registry.plugin('jsonschema2pojo').coordinate())
        project.dependencies.add('implementation', registry.library('jooq-core').notation())
        project.dependencies.add('implementation', registry.library('jooq-meta').notation())
    }
}
