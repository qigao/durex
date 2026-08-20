package com.github.durex.gradle.settings

import org.gradle.api.GradleException

final class ProjectRegistry {
    private final Map<String, ProjectSpec> byPath = new LinkedHashMap<>()
    private final Map<File, ProjectSpec> byDirectory = new LinkedHashMap<>()

    ProjectRegistry(Collection<ProjectSpec> projects) {
        projects.each { add(it) }
    }

    private void add(ProjectSpec spec) {
        ProjectSpec pathConflict = byPath.get(spec.gradlePath)
        if (pathConflict != null) {
            fail("duplicate logical project '${spec.gradlePath}' for '${pathConflict.directory}' and '${spec.directory}'")
        }
        ProjectSpec directoryConflict = byDirectory.get(spec.directory)
        if (directoryConflict != null) {
            fail("physical directory '${spec.directory}' mapped to both '${directoryConflict.gradlePath}' and '${spec.gradlePath}'")
        }
        byPath.put(spec.gradlePath, spec)
        byDirectory.put(spec.directory, spec)
    }

    Collection<ProjectSpec> projects() {
        byPath.values().toList().sort { a, b -> a.gradlePath <=> b.gradlePath }
    }

    ProjectSpec project(String path) {
        byPath.get(path.startsWith(':') ? path : ":${path}")
    }

    private static void fail(String problem) {
        throw new GradleException("Durex module discovery error\nProblem: ${problem}")
    }
}
