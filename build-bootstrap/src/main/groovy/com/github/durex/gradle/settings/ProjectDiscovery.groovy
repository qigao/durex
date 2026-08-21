package com.github.durex.gradle.settings

import org.gradle.api.GradleException
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable

final class ProjectDiscovery {
    private static final Set<String> ROOT_KEYS = ['discovery', 'module'] as Set
    private static final Set<String> DISCOVERY_KEYS = ['mode', 'roots', 'exclude'] as Set
    private static final Set<String> MODULE_KEYS = ['name', 'path', 'build-file'] as Set
    private static final Set<String> BLOCKED_DIRECTORIES = ['build', 'src', '.gradle', '.git', 'out', 'target'] as Set
    private static final List<String> BUILD_FILES = [
            'build.spring.gradle',
            'build.spring.gradle.kts',
            'build.gradle',
            'build.gradle.kts'
    ]

    static ProjectRegistry discover(File repositoryRoot, File modulesManifest) {
        File root = repositoryRoot.canonicalFile
        File manifest = modulesManifest.canonicalFile

        def parsed = null
        if (manifest.isFile()) {
            parsed = Toml.parse(manifest.toPath())
            if (parsed.hasErrors()) {
                fail("cannot parse ${manifest}: ${parsed.errors().collect { it.toString() }.join('; ')}")
            }
            rejectUnknown(parsed.keySet(), ROOT_KEYS, 'root')
        }

        TomlTable discovery = parsed?.getTable('discovery')
        String mode = 'auto'
        List<String> roots = ['core']
        List<String> excludes = []
        if (discovery != null) {
            rejectUnknown(discovery.keySet(), DISCOVERY_KEYS, 'discovery')
            Object modeNode = discovery.get('mode')
            if (modeNode != null) {
                if (!(modeNode instanceof String)) fail('discovery.mode must be a string')
                mode = modeNode as String
            }
            if (!['auto', 'manual', 'strict-auto'].contains(mode)) {
                fail("unsupported discovery mode '${mode}'")
            }
            if (discovery.get('roots') != null) roots = stringArray(discovery.get('roots'), 'discovery.roots')
            if (discovery.get('exclude') != null) excludes = stringArray(discovery.get('exclude'), 'discovery.exclude')
        }

        List<File> excludedDirectories = excludes.collect { resolveInside(root, it, 'exclude') }
        Map<File, ProjectSpec> selected = new LinkedHashMap<>()

        if (mode != 'manual') {
            roots.each { rootPath ->
                File scanRoot = resolveInside(root, rootPath, 'discovery root')
                if (!scanRoot.isDirectory()) {
                    fail("discovery root does not exist: ${scanRoot}")
                }
                scan(scanRoot, scanRoot, root, excludedDirectories, mode == 'strict-auto', selected)
            }
        }

        Set<File> manualDirectories = new LinkedHashSet<>()
        TomlArray manual = parsed?.getArray('module')
        if (manual != null) {
            for (int i = 0; i < manual.size(); i++) {
                Object node = manual.get(i)
                if (!(node instanceof TomlTable)) fail("module[${i}] must be a table")
                TomlTable entry = node as TomlTable
                rejectUnknown(entry.keySet(), MODULE_KEYS, "module[${i}]")
                String name = requiredString(entry, 'name', "module[${i}]")
                String path = requiredString(entry, 'path', "module[${i}]")
                File directory = resolveInside(root, path, "module '${name}'")
                if (isExcluded(directory, excludedDirectories)) {
                    continue
                }
                if (!directory.isDirectory()) {
                    fail("module '${name}' directory does not exist: ${directory}")
                }
                if (!manualDirectories.add(directory)) {
                    fail("module directory declared more than once: ${directory}")
                }

                Object buildFileNode = entry.get('build-file')
                if (buildFileNode != null && !(buildFileNode instanceof String)) {
                    fail("module '${name}' build-file must be a string")
                }
                String explicitBuildFile = buildFileNode as String
                ProjectSpec automatic = selected.remove(directory)
                String buildFile = explicitBuildFile ?: automatic?.buildFile ?: detectBuildFile(directory, "module '${name}'")
                if (!new File(directory, buildFile).isFile()) {
                    fail("module '${name}' build file does not exist: ${new File(directory, buildFile)}")
                }
                selected.put(directory, new ProjectSpec(toGradlePath(name), directory, 'manual', buildFile))
            }
        }

        if (mode == 'manual' && manual == null) {
            fail("manual discovery mode requires at least one [[module]] declaration")
        }

        new ProjectRegistry(selected.values())
    }

    private static void scan(
            File directory,
            File scanRoot,
            File repositoryRoot,
            List<File> excludes,
            boolean strict,
            Map<File, ProjectSpec> selected) {
        if (isExcluded(directory, excludes)) return

        String buildFile = preferredBuildFile(directory)
        if (buildFile != null) {
            String name = deriveName(repositoryRoot, scanRoot, directory, strict)
            File canonical = directory.canonicalFile
            if (!selected.containsKey(canonical)) {
                selected.put(canonical, new ProjectSpec(toGradlePath(name), canonical, 'auto', buildFile))
            }
        }

        File[] children = directory.listFiles()
        if (children == null) return
        children.findAll { it.isDirectory() }
                .sort { a, b -> a.name <=> b.name }
                .each { child ->
                    if (!BLOCKED_DIRECTORIES.contains(child.name)) {
                        scan(child, scanRoot, repositoryRoot, excludes, strict, selected)
                    }
                }
    }

    private static String deriveName(File repositoryRoot, File scanRoot, File directory, boolean strict) {
        List<String> repoSegments = segments(repositoryRoot.toPath().relativize(directory.toPath()).toString())
        if (repoSegments.size() == 2 && repoSegments[0] == 'core') {
            return repoSegments[1]
        }
        if (repoSegments.size() == 3 && repoSegments[0] == 'core' && repoSegments[1] == 'shared') {
            return "shared-${repoSegments[2]}"
        }
        if (repoSegments.size() == 4 && repoSegments[0] == 'core' && repoSegments[1] == 'schema') {
            return "${repoSegments[2]}-${repoSegments[3]}"
        }
        if (strict) {
            fail("strict-auto cannot infer a canonical project name for '${repositoryRoot.toPath().relativize(directory.toPath())}'")
        }
        List<String> relativeToRoot = segments(scanRoot.toPath().relativize(directory.toPath()).toString())
        if (relativeToRoot.isEmpty()) {
            fail("cannot derive project name for discovery root '${scanRoot}'")
        }
        relativeToRoot.join('-')
    }

    private static List<String> segments(String path) {
        path.replace('\\', '/').split('/').findAll { !it.isEmpty() }
    }

    private static String preferredBuildFile(File directory) {
        BUILD_FILES.find { new File(directory, it).isFile() }
    }

    private static String detectBuildFile(File directory, String subject) {
        String found = preferredBuildFile(directory)
        if (found == null) {
            fail("${subject} must contain one of ${BUILD_FILES.join(', ')}")
        }
        found
    }

    private static File resolveInside(File root, String path, String subject) {
        File resolved = new File(root, path).canonicalFile
        if (!resolved.toPath().startsWith(root.toPath())) {
            fail("${subject} escapes repository root: ${path}")
        }
        resolved
    }

    private static boolean isExcluded(File directory, List<File> excludes) {
        excludes.any { excluded -> directory.canonicalFile.toPath().startsWith(excluded.toPath()) }
    }

    private static List<String> stringArray(Object node, String subject) {
        if (!(node instanceof TomlArray)) fail("${subject} must be an array of strings")
        TomlArray array = node as TomlArray
        List<String> values = []
        for (int i = 0; i < array.size(); i++) {
            Object value = array.get(i)
            if (!(value instanceof String)) fail("${subject} must contain only strings")
            values.add(value as String)
        }
        values
    }

    private static String requiredString(TomlTable table, String key, String subject) {
        Object value = table.get(key)
        if (!(value instanceof String) || (value as String).trim().isEmpty()) {
            fail("${subject}.${key} must be a non-empty string")
        }
        value as String
    }

    private static String toGradlePath(String name) {
        String normalized = name.startsWith(':') ? name.substring(1) : name
        if (normalized.isEmpty() || normalized.contains(':') || normalized.contains('/')) {
            fail("invalid logical project name '${name}'")
        }
        ":${normalized}"
    }

    private static void rejectUnknown(Set<String> actual, Set<String> allowed, String subject) {
        Set<String> unknown = new LinkedHashSet<>(actual)
        unknown.removeAll(allowed)
        if (!unknown.isEmpty()) fail("${subject} contains unsupported key(s): ${unknown.sort().join(', ')}")
    }

    private static void fail(String problem) {
        throw new GradleException("Durex module discovery error\nProblem: ${problem}")
    }
}
