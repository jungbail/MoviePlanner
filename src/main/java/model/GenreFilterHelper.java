package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utility methods for working with TMDB movie genres.
 */
public final class GenreFilterHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path DEFAULT_GENRE_JSON = Path.of("data/genre.json");
    private static final Map<Integer, String> BY_ID = loadGenreTable(DEFAULT_GENRE_JSON);

    /** Private constructor to prevent instantiation. */
    private GenreFilterHelper() {}

    /**
     * Replaces {@code currentFilteredMovies} with all movies from {@code workingList} whose genres
     * satisfy the filter.
     * <p>
     * {@link Operations#EQUALS}: movie is kept if it has at least one genre whose name equals {@code param}
     * (case-insensitive).<br>
     * {@link Operations#NOT_EQUALS}: movie is kept if it has <strong>no</strong> genre with that name
     * (excludes movies tagged with that genre).<br>
     * When chaining filters, pass the <strong>previous result</strong> as {@code workingList} on the next call.
     * If {@code workingList} and {@code currentFilteredMovies} are the same set, a snapshot is taken before
     * clearing so the second pass still sees the prior results.
     *
     * @param workingList source movies to evaluate
     * @param currentFilteredMovies set to replace with matches (cleared first)
     * @param param genre label (e.g. {@code "Action"})
     * @param operator {@link Operations#EQUALS} or {@link Operations#NOT_EQUALS}
     */
    public static void genreFilter(Set<Movie> workingList, Set<Movie> currentFilteredMovies,
                                   String param, Operations operator) {
        String target = param.toLowerCase(Locale.ROOT);
        List<Movie> snapshot = new ArrayList<>(workingList);
        currentFilteredMovies.clear();
        for (Movie movie : snapshot) {
            if (matchesGenreFilter(movie, target, operator)) {
                currentFilteredMovies.add(movie);
            }
        }
    }

    private static boolean matchesGenreFilter(Movie movie, String target, Operations operator) {
        return switch (operator) {
            case EQUALS -> hasGenreNamed(movie, target);
            case NOT_EQUALS -> !hasGenreNamed(movie, target);
            default -> throw new IllegalArgumentException("Invalid operator " + operator + " for genre name");
        };
    }

    /** True if any TMDB genre id on this movie maps to {@code nameLower} (already lower-case). */
    private static boolean hasGenreNamed(Movie movie, String nameLower) {
        for (Integer genreId : movie.genreIds()) {
            String genreName = getGenreName(genreId);
            if (genreName != null && genreName.toLowerCase(Locale.ROOT).equals(nameLower)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGenreContaining(Movie movie, String target) {
        for (Integer genreId : movie.genreIds()) {
            String genreName = getGenreName(genreId);
            if (genreName != null && genreName.toLowerCase(Locale.ROOT).contains(target)) {
                return true;
            }
        }
        return false;
    }

    private static Map<Integer, String> loadGenreTable(Path jsonFile) {
        try {
            JsonNode root = MAPPER.readTree(jsonFile.toFile());
            JsonNode entries = root.isArray() ? root : root.path("genres");
            if (!entries.isArray()) {
                throw new IOException("Expected a JSON array or an object with a \"genres\" array: " + jsonFile);
            }

            Map<Integer, String> map = new HashMap<>();
            for (JsonNode node : entries) {
                if (node != null && node.hasNonNull("id") && node.hasNonNull("name")) {
                    map.put(node.get("id").asInt(), node.get("name").asText());
                }
            }
            return Collections.unmodifiableMap(map);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load genres from " + jsonFile, e);
        }
    }

    /**
     * Looks up the genre label for a TMDB genre id, e.g. {@code 28 -> "Action"}.
     *
     * @param id TMDB genre id
     * @return the display name, or {@code null} if unknown
     */
    public static String getGenreName(int id) {
        return BY_ID.get(id);
    }

    /**
     * All genre names from the loaded TMDB table ({@code data/genre.json}), sorted for UI display.
     */
    public static List<String> allGenreNamesSorted() {
        List<String> names = new ArrayList<>(BY_ID.values());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(names);
    }

}
