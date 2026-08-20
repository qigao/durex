package com.github.durex.gradle.capability

import com.github.durex.gradle.DurexConfigurationException

final class CapabilityRegistry {
    private final Map<String, CapabilitySpec> specs = new LinkedHashMap<>()

    synchronized void register(CapabilitySpec spec) {
        CapabilitySpec existing = specs.get(spec.id)
        if (existing == null) {
            specs.put(spec.id, spec)
            return
        }
        if (existing != spec) {
            throw new DurexConfigurationException(
                    'Durex configuration error\n' +
                    "Capability: ${spec.id}\n" +
                    "Problem: capability '${spec.id}' is already registered with a different definition")
        }
    }

    synchronized CapabilitySpec get(String id) {
        specs.get(id)
    }

    synchronized boolean contains(String id) {
        specs.containsKey(id)
    }

    synchronized Collection<CapabilitySpec> all() {
        Collections.unmodifiableList(specs.values().toList().sort { a, b -> a.id <=> b.id })
    }
}
