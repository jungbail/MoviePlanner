package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import util.TmdbMovieFetcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class
 * Loads a {@link Set} of {@link Movie} from a JSON file whose root value is an array of movie objects
 * (the format written by {@link TmdbMovieFetcher}).
 */
public final class MovieSetLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Private constructor to prevent instantiation. */
    private MovieSetLoader() {}

    /**
     * Reads the file from the filesystem (not the classpath). When running Gradle or the IDE with the
     * project root as the working directory, use {@code Path.of("data/movies.json")}.
     *
     * @param jsonFile path to the JSON array file
     * @return a set containing all movies (insertion order matches the array — {@link HashSet})
     */
    public static Set<Movie> loadMovies(Path jsonFile) {
        try {
            Movie[] movies = MAPPER.readValue(jsonFile.toFile(), Movie[].class);
            return new HashSet<>(Arrays.asList(movies));
        } catch (IOException e) {
            throw new RuntimeException("Error loading movies from file: " + jsonFile, e);
        }
    }

    /**
     * Same as {@link #loadMovies(Path)} using a filesystem path string (e.g. {@code "data/movies.json"}).
     */
    public static Set<Movie> loadMovies(String jsonFilePath) {
        return loadMovies(Path.of(jsonFilePath));
    }
}
