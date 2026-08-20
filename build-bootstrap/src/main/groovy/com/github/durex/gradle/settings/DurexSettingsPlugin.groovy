package com.github.durex.gradle.settings

import com.github.durex.gradle.manifest.DependencyRegistryService
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DurexSettingsPlugin implements Plugin<Settings> {
    @Override
    void apply(Settings settings) {
        DurexSettingsExtension extension = settings.extensions.create('durexSettings', DurexSettingsExtension)
        extension.repositoryRoot.convention(settings.layout.settingsDirectory)
        extension.dependencyManifest.convention(extension.repositoryRoot.file('gradle/dependencies/durex.toml'))
        extension.modulesManifest.convention(extension.repositoryRoot.file('gradle/modules.toml'))
        extension.moduleDiscovery.convention(true)

        def serviceHolder = [provider: null]
        settings.pluginManagement.resolutionStrategy.eachPlugin { details ->
            def provider = serviceHolder.provider
            if (provider == null) return
            def managed = provider.get().pluginByGradleId(details.requested.id.id)
            if (managed == null) return
            String requested = details.requested.version
            if (requested && requested != managed.version) {
                throw new GradleException(
                        "Durex dependency version conflict\n" +
                        "Plugin: ${managed.id}\nRequested: ${requested}\nManaged: ${managed.version}")
            }
            if (managed.module) {
                details.useModule(managed.coordinate())
            } else {
                details.useVersion(managed.version)
            }
        }

        settings.gradle.settingsEvaluated {
            def serviceProvider = settings.gradle.sharedServices.registerIfAbsent(
                    'durexDependencyRegistry', DependencyRegistryService) { spec ->
                spec.parameters.manifestFile.set(extension.dependencyManifest)
            }
            serviceHolder.provider = serviceProvider

            // Force one validated parse during settings evaluation so manifest errors fail fast.
            serviceProvider.get().javaVersion()

            settings.gradle.rootProject { root ->
                root.tasks.register('durexDependencies') {
                    group = 'Durex'
                    description = 'Print Durex dependency manifest diagnostics.'
                    doLast {
                        def service = serviceProvider.get()
                        println "Manifest: ${extension.dependencyManifest.get().asFile.canonicalPath}"
                        println "Java: ${service.javaVersion()}"
                        println "Platform spring: ${service.platform('spring').coordinate()}"
                        service.plugins().sort { a, b -> a.alias <=> b.alias }.each { plugin ->
                            if (['spring-boot', 'graalvm-native', 'jooq-codegen'].contains(plugin.alias)) {
                                println "Plugin ${plugin.alias}: ${plugin.version}"
                            }
                        }
                    }
                }
            }

            // Task 2 extends this point with project discovery.
            if (!extension.moduleDiscovery.get()) return
        }
    }
}
