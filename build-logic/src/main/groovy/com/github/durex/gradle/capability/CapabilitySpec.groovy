package com.github.durex.gradle.capability

import com.github.durex.gradle.ModuleKind
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
final class CapabilitySpec {
    final String id
    final Set<ModuleKind> allowedModules
    final Set<String> requires
    final Set<String> conflicts
    final List<DependencyBinding> dependencies
    final Set<String> externalPluginAliases

    private CapabilitySpec(
            String id,
            Set<ModuleKind> allowedModules,
            Set<String> requires,
            Set<String> conflicts,
            List<DependencyBinding> dependencies,
            Set<String> externalPluginAliases) {
        this.id = id
        this.allowedModules = Collections.unmodifiableSet(new LinkedHashSet<>(allowedModules))
        this.requires = Collections.unmodifiableSet(new TreeSet<>(requires))
        this.conflicts = Collections.unmodifiableSet(new TreeSet<>(conflicts))
        List<DependencyBinding> sortedDependencies = new ArrayList<>(dependencies)
        Collections.sort(sortedDependencies)
        this.dependencies = Collections.unmodifiableList(sortedDependencies)
        this.externalPluginAliases = Collections.unmodifiableSet(new TreeSet<>(externalPluginAliases))
    }

    static Builder builder(String id) {
        new Builder(id)
    }

    static final class Builder {
        private final String id
        private final Set<ModuleKind> allowedModules = new LinkedHashSet<>()
        private final Set<String> requires = new LinkedHashSet<>()
        private final Set<String> conflicts = new LinkedHashSet<>()
        private final List<DependencyBinding> dependencies = []
        private final Set<String> externalPluginAliases = new LinkedHashSet<>()

        Builder(String id) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException('capability id must be non-empty')
            }
            this.id = id
        }

        Builder allow(ModuleKind... kinds) {
            allowedModules.addAll(Arrays.asList(kinds))
            this
        }

        Builder require(String... capabilityIds) {
            requires.addAll(Arrays.asList(capabilityIds))
            this
        }

        Builder conflict(String... capabilityIds) {
            conflicts.addAll(Arrays.asList(capabilityIds))
            this
        }

        Builder dependency(String configuration, String libraryAlias) {
            dependencies.add(new DependencyBinding(configuration, libraryAlias))
            this
        }

        Builder externalPlugin(String pluginAlias) {
            externalPluginAliases.add(pluginAlias)
            this
        }

        CapabilitySpec build() {
            new CapabilitySpec(id, allowedModules, requires, conflicts, dependencies, externalPluginAliases)
        }
    }
}
