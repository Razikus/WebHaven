package tech.razikus.headlesshaven;

import haven.ResCache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class SimpleResourceCache implements ResCache {
    private static final String CACHE_DIR = "cache";
    private static final Pattern UNSAFE_CHARS = Pattern.compile("[^a-zA-Z0-9.-]");

    public SimpleResourceCache() {
        // Create cache directory if it doesn't exist
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }
        } catch (IOException e) {
            System.err.println("Failed to create cache directory: " + e.getMessage());
        }
    }

    private static String getSafeFileName(String name) {
        // Replace slashes with dashes first
        String safe = name.replace('/', '-').replace('\\', '-');
        // Replace any other unsafe characters with dashes
        safe = UNSAFE_CHARS.matcher(safe).replaceAll("-");
        // Collapse multiple dashes into one
        safe = safe.replaceAll("-+", "-");
        // Remove leading/trailing dashes
        safe = safe.replaceAll("^-+|-+$", "");
        return safe;
    }

    @Override
    public OutputStream store(String name) throws IOException {
        Path cacheDir = Paths.get(CACHE_DIR);
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
        String safeFileName = getSafeFileName(name);
        Path filePath = cacheDir.resolve(safeFileName + ".cache");

        // Create and return a buffered output stream
        return new BufferedOutputStream(Files.newOutputStream(filePath));
    }

    @Override
    public InputStream fetch(String name) throws IOException {
        Path cacheDir = Paths.get(CACHE_DIR);
        String safeFileName = getSafeFileName(name);
        Path filePath = cacheDir.resolve(safeFileName + ".cache");

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Cache entry not found: " + name);
        }
        // Create and return a buffered input stream
        return new BufferedInputStream(Files.newInputStream(filePath));
    }

    // Optional: Add a method to clear the cache
    public void clearCache() {
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (Files.exists(cacheDir)) {
                Files.walk(cacheDir)
                        .filter(path -> !path.equals(cacheDir))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("Failed to delete cache file: " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }
}