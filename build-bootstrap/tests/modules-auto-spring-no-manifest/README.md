# Manifest-free Spring module discovery fixture

This fixture verifies that `durex.settings` can discover `build.spring.gradle` modules without a `modules.toml`, while treating directories that only organize discoverable child modules as structural parents rather than projects.
