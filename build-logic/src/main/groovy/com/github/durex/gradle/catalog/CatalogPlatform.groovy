package com.github.durex.gradle.catalog

final class CatalogPlatform {
    final String alias
    final String module
    final String version

    CatalogPlatform(String alias, String module, String version) {
        this.alias = alias
        this.module = module
        this.version = version
    }

    String coordinate() {
        "${module}:${version}"
    }
}
