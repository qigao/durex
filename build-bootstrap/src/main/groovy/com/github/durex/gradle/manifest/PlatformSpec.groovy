package com.github.durex.gradle.manifest

final class PlatformSpec {
    final String id
    final String module
    final String version

    PlatformSpec(String id, String module, String version) {
        this.id = id
        this.module = module
        this.version = version
    }

    String coordinate() {
        "${module}:${version}"
    }
}
