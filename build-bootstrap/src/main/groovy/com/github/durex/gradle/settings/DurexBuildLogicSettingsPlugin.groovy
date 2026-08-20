package com.github.durex.gradle.settings

import com.github.durex.gradle.manifest.DependencyRegistryService
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DurexBuildLogicSettingsPlugin implements Plugin<Settings> {
    @Override
    void apply(Settings settings) {
        DurexBuildLogicSettingsExtension extension = settings.extensions.create(
                'durexBuildLogicSettings', DurexBuildLogicSettingsExtension)
        extension.repositoryRoot.convention(settings.layout.settingsDirectory)
        extension.dependencyManifest.convention(
                extension.repositoryRoot.file('gradle/dependencies/durex.toml'))

        settings.gradle.settingsEvaluated {
            def serviceProvider = settings.gradle.sharedServices.registerIfAbsent(
                    'durexDependencyRegistry', DependencyRegistryService) { spec ->
                spec.parameters.manifestFile.set(extension.dependencyManifest)
            }
            serviceProvider.get().javaVersion()
        }
    }
}
