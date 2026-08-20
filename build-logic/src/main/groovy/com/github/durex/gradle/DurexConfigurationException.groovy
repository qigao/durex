package com.github.durex.gradle

import org.gradle.api.GradleException

class DurexConfigurationException extends GradleException {
    DurexConfigurationException(String message) {
        super(message)
    }

    DurexConfigurationException(String message, Throwable cause) {
        super(message, cause)
    }

    static DurexConfigurationException moduleTypeConflict(
            String projectPath,
            ModuleKind existing,
            ModuleKind requested) {
        new DurexConfigurationException(
                'Durex configuration error\n' +
                "Project: ${projectPath}\n" +
                'Problem: module type conflict\n' +
                "Existing: ${existing}\n" +
                "Requested: ${requested}")
    }
}
