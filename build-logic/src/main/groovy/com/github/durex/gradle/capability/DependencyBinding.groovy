package com.github.durex.gradle.capability

import groovy.transform.Immutable

@Immutable
class DependencyBinding implements Comparable<DependencyBinding> {
    String configuration
    String libraryAlias

    @Override
    int compareTo(DependencyBinding other) {
        int byConfiguration = configuration <=> other.configuration
        byConfiguration != 0 ? byConfiguration : libraryAlias <=> other.libraryAlias
    }
}
