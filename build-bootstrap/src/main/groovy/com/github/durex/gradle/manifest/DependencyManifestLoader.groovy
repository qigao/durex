package com.github.durex.gradle.manifest

import org.gradle.api.GradleException
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable

final class DependencyManifestLoader {
    private static final Set<String> ROOT_KEYS = ['include', 'java', 'versions', 'platforms', 'libraries', 'plugins'] as Set

    static DependencyRegistry load(File rootManifest) {
        State state = new State()
        loadFile(rootManifest.canonicalFile, state, new LinkedHashSet<File>())
        if (state.javaVersion == null) {
            fail(rootManifest, null, null, 'missing [java].version')
        }

        Map<String, VersionSpec> versions = new LinkedHashMap<>()
        state.versionValues.each { id, raw -> versions.put(id, new VersionSpec(id, raw.value as String)) }

        Map<String, PlatformSpec> platforms = new LinkedHashMap<>()
        state.platformValues.each { id, raw ->
            String version = resolveVersion(raw, versions, raw.file as File, 'Platform', id)
            platforms.put(id, new PlatformSpec(id, raw.module as String, version))
        }

        Map<String, LibrarySpec> libraries = new LinkedHashMap<>()
        state.libraryValues.each { id, raw ->
            int owners = (raw.explicitVersion ? 1 : 0) + (raw.versionRef ? 1 : 0) + (raw.platform ? 1 : 0)
            if (owners != 1) {
                String problem = owners == 0
                        ? 'exactly one version owner is required: version, version.ref, or platform'
                        : 'platform and explicit version ownership are mutually exclusive'
                fail(raw.file as File, 'Library', id, problem)
            }
            String version = null
            if (raw.platform) {
                if (!platforms.containsKey(raw.platform as String)) {
                    fail(raw.file as File, 'Library', id, "unknown platform '${raw.platform}'")
                }
            } else {
                version = resolveVersion(raw, versions, raw.file as File, 'Library', id)
            }
            libraries.put(id, new LibrarySpec(id, raw.module as String, version, raw.platform as String))
        }

        Map<String, PluginSpec> plugins = new LinkedHashMap<>()
        Map<String, PluginSpec> pluginsByGradleId = new LinkedHashMap<>()
        state.pluginValues.each { alias, raw ->
            int owners = (raw.explicitVersion ? 1 : 0) + (raw.versionRef ? 1 : 0)
            if (owners != 1) {
                fail(raw.file as File, 'Plugin', alias, 'exactly one version owner is required: version or version.ref')
            }
            String version = resolveVersion(raw, versions, raw.file as File, 'Plugin', alias)
            PluginSpec spec = new PluginSpec(alias, raw.id as String, raw.module as String, version)
            PluginSpec previous = pluginsByGradleId.put(spec.id, spec)
            if (previous != null) {
                fail(raw.file as File, 'Plugin', alias, "duplicate Gradle plugin id '${spec.id}' (already defined by '${previous.alias}')")
            }
            plugins.put(alias, spec)
        }

        new DependencyRegistry(state.javaVersion as int, versions, platforms, libraries, plugins, pluginsByGradleId)
    }

    private static void loadFile(File file, State state, LinkedHashSet<File> stack) {
        File canonical = file.canonicalFile
        if (stack.contains(canonical)) {
            String cycle = (stack + canonical).collect { it.name }.join(' -> ')
            fail(canonical, null, null, "include cycle detected: ${cycle}")
        }
        if (state.loaded.contains(canonical)) {
            return
        }
        if (!canonical.isFile()) {
            fail(canonical, null, null, 'manifest file does not exist')
        }

        def parsed = Toml.parse(canonical.toPath())
        if (parsed.hasErrors()) {
            fail(canonical, null, null, parsed.errors().collect { it.toString() }.join('; '))
        }
        rejectUnknownKeys(parsed.keySet(), ROOT_KEYS, canonical, null, null)

        stack.add(canonical)
        Object includeNode = parsed.get('include')
        if (includeNode != null) {
            if (!(includeNode instanceof TomlArray)) {
                fail(canonical, null, null, 'include must be an array of file names')
            }
            TomlArray includes = includeNode as TomlArray
            for (int i = 0; i < includes.size(); i++) {
                Object value = includes.get(i)
                if (!(value instanceof String)) {
                    fail(canonical, null, null, 'include entries must be strings')
                }
                loadFile(new File(canonical.parentFile, value as String), state, stack)
            }
        }

        TomlTable javaTable = parsed.getTable('java')
        if (javaTable != null) {
            rejectUnknownKeys(javaTable.keySet(), ['version'] as Set, canonical, 'Java', 'java')
            Object node = javaTable.get('version')
            if (!(node instanceof Number)) {
                fail(canonical, 'Java', 'java', 'version must be an integer')
            }
            if (state.javaVersion != null) {
                fail(canonical, 'Java', 'java', 'duplicate java.version')
            }
            state.javaVersion = (node as Number).intValue()
        }

        parseVersions(parsed.getTable('versions'), canonical, state)
        parsePlatforms(parsed.getTable('platforms'), canonical, state)
        parseLibraries(parsed.getTable('libraries'), canonical, state)
        parsePlugins(parsed.getTable('plugins'), canonical, state)

        stack.remove(canonical)
        state.loaded.add(canonical)
    }

    private static void parseVersions(TomlTable table, File file, State state) {
        if (table == null) return
        table.keySet().each { id ->
            Object value = table.get(id)
            if (!(value instanceof String)) {
                fail(file, 'Version', id, 'value must be a string')
            }
            putUnique(state.versionValues, id, [value: value, file: file], file, 'Version')
        }
    }

    private static void parsePlatforms(TomlTable table, File file, State state) {
        if (table == null) return
        table.keySet().each { id ->
            TomlTable value = requireTable(table, id, file, 'Platform')
            rejectUnknownKeys(value.keySet(), ['module', 'version'] as Set, file, 'Platform', id)
            String module = requireString(value, 'module', file, 'Platform', id)
            validateModule(module, file, 'Platform', id)
            Map version = readVersion(value, file, 'Platform', id)
            if ((version.explicit ? 1 : 0) + (version.ref ? 1 : 0) != 1) {
                fail(file, 'Platform', id, 'exactly one version owner is required: version or version.ref')
            }
            putUnique(state.platformValues, id, [module: module, explicitVersion: version.explicit, versionRef: version.ref, file: file], file, 'Platform')
        }
    }

    private static void parseLibraries(TomlTable table, File file, State state) {
        if (table == null) return
        table.keySet().each { id ->
            TomlTable value = requireTable(table, id, file, 'Library')
            rejectUnknownKeys(value.keySet(), ['module', 'version', 'platform'] as Set, file, 'Library', id)
            String module = requireString(value, 'module', file, 'Library', id)
            validateModule(module, file, 'Library', id)
            Map version = readVersion(value, file, 'Library', id)
            Object platformNode = value.get('platform')
            if (platformNode != null && !(platformNode instanceof String)) {
                fail(file, 'Library', id, 'platform must be a string')
            }
            putUnique(state.libraryValues, id, [module: module, explicitVersion: version.explicit, versionRef: version.ref, platform: platformNode, file: file], file, 'Library')
        }
    }

    private static void parsePlugins(TomlTable table, File file, State state) {
        if (table == null) return
        table.keySet().each { alias ->
            TomlTable value = requireTable(table, alias, file, 'Plugin')
            rejectUnknownKeys(value.keySet(), ['id', 'module', 'version'] as Set, file, 'Plugin', alias)
            String id = requireString(value, 'id', file, 'Plugin', alias)
            Object moduleNode = value.get('module')
            if (moduleNode != null && !(moduleNode instanceof String)) {
                fail(file, 'Plugin', alias, 'module must be a string')
            }
            if (moduleNode != null) validateModule(moduleNode as String, file, 'Plugin', alias)
            Map version = readVersion(value, file, 'Plugin', alias)
            putUnique(state.pluginValues, alias, [id: id, module: moduleNode, explicitVersion: version.explicit, versionRef: version.ref, file: file], file, 'Plugin')
        }
    }

    private static Map readVersion(TomlTable table, File file, String kind, String id) {
        Object node = table.get('version')
        if (node == null) return [explicit: null, ref: null]
        if (node instanceof String) return [explicit: node, ref: null]
        if (node instanceof TomlTable) {
            TomlTable nested = node as TomlTable
            rejectUnknownKeys(nested.keySet(), ['ref'] as Set, file, kind, id)
            String ref = requireString(nested, 'ref', file, kind, id)
            return [explicit: null, ref: ref]
        }
        fail(file, kind, id, 'version must be a string or version.ref table')
        [:]
    }

    private static String resolveVersion(Map raw, Map<String, VersionSpec> versions, File file, String kind, String id) {
        if (raw.explicitVersion) return raw.explicitVersion as String
        String ref = raw.versionRef as String
        VersionSpec spec = versions.get(ref)
        if (spec == null) {
            fail(file, kind, id, "unknown version.ref '${ref}'")
        }
        spec.value
    }

    private static TomlTable requireTable(TomlTable parent, String id, File file, String kind) {
        Object node = parent.get(id)
        if (!(node instanceof TomlTable)) {
            fail(file, kind, id, 'definition must be a table')
        }
        node as TomlTable
    }

    private static String requireString(TomlTable table, String key, File file, String kind, String id) {
        Object value = table.get(key)
        if (!(value instanceof String) || (value as String).trim().isEmpty()) {
            fail(file, kind, id, "${key} must be a non-empty string")
        }
        value as String
    }

    private static void validateModule(String module, File file, String kind, String id) {
        String[] parts = module.split(':', -1)
        if (parts.length != 2 || parts.any { it.trim().isEmpty() }) {
            fail(file, kind, id, "malformed module coordinate '${module}', expected group:name")
        }
    }

    private static void rejectUnknownKeys(Set<String> actual, Set<String> allowed, File file, String kind, String id) {
        Set<String> unknown = new LinkedHashSet<>(actual)
        unknown.removeAll(allowed)
        if (!unknown.isEmpty()) {
            fail(file, kind, id, "unsupported key(s): ${unknown.sort().join(', ')}")
        }
    }

    private static void putUnique(Map values, String id, Object value, File file, String kind) {
        if (values.containsKey(id)) {
            fail(file, kind, id, "duplicate ${kind.toLowerCase()} id '${id}'")
        }
        values.put(id, value)
    }

    private static void fail(File file, String kind, String id, String problem) {
        List<String> lines = ['Durex dependency manifest error', "File: ${file.canonicalPath}"]
        if (kind != null && id != null) lines.add("${kind}: ${id}")
        lines.add("Problem: ${problem}")
        throw new GradleException(lines.join('\n'))
    }

    private static final class State {
        Integer javaVersion
        final Set<File> loaded = new LinkedHashSet<>()
        final Map<String, Map> versionValues = new LinkedHashMap<>()
        final Map<String, Map> platformValues = new LinkedHashMap<>()
        final Map<String, Map> libraryValues = new LinkedHashMap<>()
        final Map<String, Map> pluginValues = new LinkedHashMap<>()
    }
}
