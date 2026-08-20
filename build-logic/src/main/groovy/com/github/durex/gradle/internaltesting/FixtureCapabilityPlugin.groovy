package com.github.durex.gradle.internaltesting

import com.github.durex.gradle.capability.CapabilitySpec
import com.github.durex.gradle.capability.DurexCapabilitySupport
import org.gradle.api.Plugin
import org.gradle.api.Project

class FixtureCapabilityPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('durex.module')
        DurexCapabilitySupport.registerAndEnable(
                project,
                'com.acme.durex.fixture',
                CapabilitySpec.builder('fixture').build())
    }
}
