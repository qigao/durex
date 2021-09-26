package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
*/
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final CommonsLibraryAccessors laccForCommonsLibraryAccessors = new CommonsLibraryAccessors(owner);
    private final GroovyLibraryAccessors laccForGroovyLibraryAccessors = new GroovyLibraryAccessors(owner);
    private final QuarkusLibraryAccessors laccForQuarkusLibraryAccessors = new QuarkusLibraryAccessors(owner);
    private final WiremockLibraryAccessors laccForWiremockLibraryAccessors = new WiremockLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(providers, config);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers) {
        super(config, providers);
    }

        /**
         * Creates a dependency provider for guava (com.google.guava:guava)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getGuava() { return create("guava"); }

        /**
         * Creates a dependency provider for jackson (com.fasterxml.jackson:jackson-bom)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJackson() { return create("jackson"); }

        /**
         * Creates a dependency provider for logback (ch.qos.logback:logback-classic)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLogback() { return create("logback"); }

    /**
     * Returns the group of libraries at commons
     */
    public CommonsLibraryAccessors getCommons() { return laccForCommonsLibraryAccessors; }

    /**
     * Returns the group of libraries at groovy
     */
    public GroovyLibraryAccessors getGroovy() { return laccForGroovyLibraryAccessors; }

    /**
     * Returns the group of libraries at quarkus
     */
    public QuarkusLibraryAccessors getQuarkus() { return laccForQuarkusLibraryAccessors; }

    /**
     * Returns the group of libraries at wiremock
     */
    public WiremockLibraryAccessors getWiremock() { return laccForWiremockLibraryAccessors; }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() { return vaccForVersionAccessors; }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() { return baccForBundleAccessors; }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() { return paccForPluginAccessors; }

    public static class CommonsLibraryAccessors extends SubDependencyFactory {

        public CommonsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for lang3 (org.apache.commons:commons-lang3)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLang3() { return create("commons.lang3"); }

    }

    public static class GroovyLibraryAccessors extends SubDependencyFactory {

        public GroovyLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (org.codehaus.groovy:groovy)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() { return create("groovy.core"); }

            /**
             * Creates a dependency provider for json (org.codehaus.groovy:groovy-json)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJson() { return create("groovy.json"); }

            /**
             * Creates a dependency provider for nio (org.codehaus.groovy:groovy-nio)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getNio() { return create("groovy.nio"); }

    }

    public static class QuarkusLibraryAccessors extends SubDependencyFactory {

        public QuarkusLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for bom (io.quarkus.platform:quarkus-bom)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBom() { return create("quarkus.bom"); }

    }

    public static class WiremockLibraryAccessors extends SubDependencyFactory {

        public WiremockLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jensPiegsa (com.github.jensPiegsa:wiremock-extension)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJensPiegsa() { return create("wiremock.jensPiegsa"); }

            /**
             * Creates a dependency provider for jre8 (com.github.tomakehurst:wiremock-jre8)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJre8() { return create("wiremock.jre8"); }

            /**
             * Creates a dependency provider for lanwen (ru.lanwen.wiremock:wiremock-junit5)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLanwen() { return create("wiremock.lanwen"); }

            /**
             * Creates a dependency provider for mkammerer (de.mkammerer.wiremock-junit5:wiremock-junit5)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getMkammerer() { return create("wiremock.mkammerer"); }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: groovy (3.0.9)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getGroovy() { return getVersion("groovy"); }

            /**
             * Returns the version associated to this alias: guava (30.1.1-jre)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getGuava() { return getVersion("guava"); }

            /**
             * Returns the version associated to this alias: logback (1.2.6)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getLogback() { return getVersion("logback"); }

            /**
             * Returns the version associated to this alias: quarkus (2.2.3.Final)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getQuarkus() { return getVersion("quarkus"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a dependency bundle provider for groovy which is an aggregate for the following dependencies:
             * <ul>
             *    <li>org.codehaus.groovy:groovy</li>
             *    <li>org.codehaus.groovy:groovy-json</li>
             *    <li>org.codehaus.groovy:groovy-nio</li>
             * </ul>
             * This bundle was declared in catalog libs.versions.toml
             */
            public Provider<ExternalModuleDependencyBundle> getGroovy() { return createBundle("groovy"); }

            /**
             * Creates a dependency bundle provider for wiremock which is an aggregate for the following dependencies:
             * <ul>
             *    <li>com.github.tomakehurst:wiremock-jre8</li>
             *    <li>ru.lanwen.wiremock:wiremock-junit5</li>
             *    <li>de.mkammerer.wiremock-junit5:wiremock-junit5</li>
             *    <li>com.github.jensPiegsa:wiremock-extension</li>
             * </ul>
             * This bundle was declared in catalog libs.versions.toml
             */
            public Provider<ExternalModuleDependencyBundle> getWiremock() { return createBundle("wiremock"); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for editorconfig to the plugin id 'org.ec4j.editorconfig'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getEditorconfig() { return createPlugin("editorconfig"); }

            /**
             * Creates a plugin provider for lombok to the plugin id 'io.freefair.lombok'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getLombok() { return createPlugin("lombok"); }

            /**
             * Creates a plugin provider for quarkus to the plugin id 'io.quarkus'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getQuarkus() { return createPlugin("quarkus"); }

            /**
             * Creates a plugin provider for sonarqube to the plugin id 'org.sonarqube'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getSonarqube() { return createPlugin("sonarqube"); }

            /**
             * Creates a plugin provider for spotless to the plugin id 'com.diffplug.spotless'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getSpotless() { return createPlugin("spotless"); }

            /**
             * Creates a plugin provider for spring to the plugin id 'io.spring.dependency-management'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getSpring() { return createPlugin("spring"); }

            /**
             * Creates a plugin provider for springboot to the plugin id 'org.springframework.boot'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getSpringboot() { return createPlugin("springboot"); }

    }

}
