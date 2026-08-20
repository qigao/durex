package com.github.durex.gradle.settings

final class ProjectSpec {
    final String gradlePath
    final File directory
    final String source
    final String buildFile

    ProjectSpec(String gradlePath, File directory, String source, String buildFile) {
        this.gradlePath = gradlePath.startsWith(':') ? gradlePath : ":${gradlePath}"
        this.directory = directory.canonicalFile
        this.source = source
        this.buildFile = buildFile
    }
}
