package com.github.durex.gradle.capability

import com.github.durex.gradle.DurexConfigurationException

final class CapabilityPluginRegistry {
    private final Map<String, String> mappings = new LinkedHashMap<>()

    synchronized void register(String pluginId, String capabilityId) {
        String existing = mappings.get(pluginId)
        if (existing == null) {
            mappings.put(pluginId, capabilityId)
            return
        }
        if (existing != capabilityId) {
            throw new DurexConfigurationException(
                    'Durex configuration error\n' +
                    "Gradle plugin id: ${pluginId}\n" +
                    "Problem: plugin is already mapped to primary capability '${existing}'\n" +
                    "Requested capability: ${capabilityId}")
        }
    }

    synchronized String capabilityForPlugin(String pluginId) {
        mappings.get(pluginId)
    }

    synchronized Map<String, String> all() {
        Collections.unmodifiableMap(new LinkedHashMap<>(mappings))
    }
}
