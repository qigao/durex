package com.github.durex.gradle.catalog

final class CatalogLibrary {
    final String alias
    final String module
    final String version
    final String platform

    CatalogLibrary(String alias, String module, String version, String platform) {
        this.alias = alias
        this.module = module
        this.version = version
        this.platform = platform
    }

    boolean isPlatformManaged() {
        platform != null
    }

    String notation() {
        version ? "${module}:${version}" : module
    }
}
