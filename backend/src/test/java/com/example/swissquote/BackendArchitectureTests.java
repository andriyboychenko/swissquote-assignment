package com.example.swissquote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BackendArchitectureTests {

    private static final Path MAIN_JAVA = findMainJavaDirectory();
    private static final Path ROOT_PACKAGE = MAIN_JAVA.resolve("com/example/swissquote");
    private static final Set<String> ROOT_PACKAGE_ALLOWED_FILES = Set.of(
            "SwissquoteApplication.java",
            "package-info.java"
    );

    @Test
    void backendLayerPackagesExist() {
        assertThat(ROOT_PACKAGE.resolve("domain")).isDirectory();
        assertThat(ROOT_PACKAGE.resolve("application")).isDirectory();
        assertThat(ROOT_PACKAGE.resolve("infrastructure")).isDirectory();
        assertThat(ROOT_PACKAGE.resolve("interfaces")).isDirectory();
        assertThat(ROOT_PACKAGE.resolve("config")).isDirectory();
    }

    @Test
    void rootPackageContainsOnlyApplicationEntrypoint() throws IOException {
        List<String> rootJavaFiles;
        try (var files = Files.list(ROOT_PACKAGE)) {
            rootJavaFiles = files
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(path -> path.getFileName().toString())
                    .toList();
        }

        assertThat(rootJavaFiles).allMatch(ROOT_PACKAGE_ALLOWED_FILES::contains);
    }

    @Test
    void springComponentsDoNotUseFieldInjection() throws IOException {
        List<Path> filesWithFieldInjection;
        try (var files = Files.walk(MAIN_JAVA)) {
            filesWithFieldInjection = files
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(BackendArchitectureTests::containsAutowiredAnnotation)
                    .toList();
        }

        assertThat(filesWithFieldInjection)
                .as("Use constructor injection instead of @" + Autowired.class.getSimpleName())
                .isEmpty();
    }

    private static boolean containsAutowiredAnnotation(Path path) {
        try {
            return Files.readString(path).contains("@" + Autowired.class.getSimpleName());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not inspect " + path, exception);
        }
    }

    private static Path findMainJavaDirectory() {
        Path modulePath = Path.of("src/main/java");
        if (Files.isDirectory(modulePath)) {
            return modulePath;
        }

        Path rootPath = Path.of("backend/src/main/java");
        if (Files.isDirectory(rootPath)) {
            return rootPath;
        }

        throw new IllegalStateException("Could not find backend main Java source directory");
    }
}
