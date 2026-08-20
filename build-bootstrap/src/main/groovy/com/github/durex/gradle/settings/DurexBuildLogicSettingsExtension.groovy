package com.github.durex.gradle.settings

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

abstract class DurexBuildLogicSettingsExtension {
    abstract DirectoryProperty getRepositoryRoot()
    abstract RegularFileProperty getDependencyManifest()
}
