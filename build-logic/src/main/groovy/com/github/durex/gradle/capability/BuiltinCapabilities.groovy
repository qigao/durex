package com.github.durex.gradle.capability

import com.github.durex.gradle.ModuleKind

final class BuiltinCapabilities {
    static final CapabilitySpec AOP = CapabilitySpec.builder('aop')
            .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
            .dependency('implementation', 'spring-aop')
            .build()

    static final CapabilitySpec TRANSACTION = CapabilitySpec.builder('transaction')
            .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
            .dependency('implementation', 'spring-transaction')
            .build()

    static final CapabilitySpec JPA = CapabilitySpec.builder('jpa')
            .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
            .dependency('implementation', 'spring-jpa')
            .build()

    static final CapabilitySpec JDBC = CapabilitySpec.builder('jdbc')
            .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
            .dependency('implementation', 'spring-jdbc')
            .build()

    static final CapabilitySpec JOOQ = CapabilitySpec.builder('jooq')
            .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
            .dependency('implementation', 'spring-jooq')
            .build()

    static final CapabilitySpec REDIS = CapabilitySpec.builder('redis')
            .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
            .dependency('implementation', 'spring-redis')
            .build()

    static final CapabilitySpec NATIVE = CapabilitySpec.builder('native')
            .allow(ModuleKind.SPRING_SERVICE)
            .externalPlugin('graalvm-native')
            .build()

    static final CapabilitySpec LOMBOK = CapabilitySpec.builder('lombok')
            .dependency('compileOnly', 'lombok')
            .dependency('annotationProcessor', 'lombok')
            .build()

    static void registerAll(CapabilityRegistry registry) {
        [AOP, TRANSACTION, JPA, JDBC, JOOQ, REDIS, NATIVE, LOMBOK].each { registry.register(it) }
    }

    private BuiltinCapabilities() {}
}
