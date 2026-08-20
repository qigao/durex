package com.github.durex.gradle.manifest

final class LibrarySpec {
    final String id
    final String module
    final String version
    final String platform

    LibrarySpec(String id, String module, String version, String platform) {
        this.id = id
        this.module = module
        this.version = version
        this.platform = platform
    }

    String notation() {
        version ? "${module}:${version}" : module
    }
}
