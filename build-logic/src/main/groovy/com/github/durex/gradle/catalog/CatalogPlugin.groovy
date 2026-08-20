package com.github.durex.gradle.catalog

final class CatalogPlugin {
    final String alias
    final String id
    final String module
    final String version

    CatalogPlugin(String alias, String id, String module, String version) {
        this.alias = alias
        this.id = id
        this.module = module
        this.version = version
    }

    String coordinate() {
        module ? "${module}:${version}" : null
    }
}
