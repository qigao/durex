package com.github.durex.gradle

import org.gradle.api.GradleException

final class DurexModuleState {
    private ModuleKind kind
    private final Set<String> activePlatforms = new TreeSet<>()
    private final Set<String> activeFeatures = new TreeSet<>()
    private final Set<String> platformConfigurations = new HashSet<>()
    private boolean nativeEnabled

    synchronized void claim(ModuleKind requested) {
        if (kind == null) {
            kind = requested
            return
        }
        if (kind != requested) {
            throw new GradleException(
                    "Durex module type conflict\nExisting: ${kind}\nRequested: ${requested}")
        }
    }

    void requireFeature(String featurePlugin, ModuleKind... allowed) {
        if (kind != null && allowed.contains(kind)) return
        String requirement = allowed.collect { kindPluginId(it) }.join(' or ')
        throw new GradleException("${featurePlugin} requires ${requirement}")
    }

    ModuleKind kind() { kind }

    synchronized boolean activatePlatform(String configuration, String platform) {
        activePlatforms.add(platform)
        platformConfigurations.add("${configuration}:${platform}")
    }

    synchronized void activateFeature(String feature) {
        activeFeatures.add(feature)
    }

    synchronized void enableNative() {
        nativeEnabled = true
    }

    Set<String> activePlatforms() { Collections.unmodifiableSet(activePlatforms) }
    Set<String> activeFeatures() { Collections.unmodifiableSet(activeFeatures) }
    boolean nativeEnabled() { nativeEnabled }

    private static String kindPluginId(ModuleKind kind) {
        switch (kind) {
            case ModuleKind.JAVA_LIBRARY: return 'durex.java-library'
            case ModuleKind.SPRING_LIBRARY: return 'durex.spring-library'
            case ModuleKind.SPRING_SERVICE: return 'durex.spring-service'
            default: return kind.name()
        }
    }
}
