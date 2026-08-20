package com.github.durex.gradle.settings

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class DurexSettingsExtension {
    abstract DirectoryProperty getRepositoryRoot()
    abstract RegularFileProperty getDependencyManifest()
    abstract RegularFileProperty getModulesManifest()
    abstract Property<Boolean> getModuleDiscovery()
}
