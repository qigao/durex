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
}
