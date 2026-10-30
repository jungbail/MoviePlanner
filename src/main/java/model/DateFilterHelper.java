package model;

import java.time.LocalDate;
import java.util.Set;

/**
 * Utility class for applying date filters to collection of Movies
 */
public final class DateFilterHelper {

    /** Private constructor to prevent instantiation. */
    private DateFilterHelper() {}

    /**
     * Applies a date-based filter rule to the working list.
     *
     * @param workingList source movies to evaluate
     * @param currentFilteredMovies target set to collect matches
     * @param param value from the filter string (YYYY-MM-DD)
     * @param operator comparison operator
     */
    public static void dateFilter(Set<Movie> workingList, Set<Movie> currentFilteredMovies,
                                    String param, Operations operator) {
        LocalDate target = LocalDate.parse(param); // Sets a target
        for (Movie movie : workingList) { // For every movie in the working list
            String raw = movie.releaseDate();
            if (raw == null || raw.isBlank()) {
                continue; // TMDB sometimes omits date; cannot compare
            }
            LocalDate value = LocalDate.parse(raw);
            if (matches(value, target, operator)) {
                currentFilteredMovies.add(movie); // Add to the current file any movies that match the filter
            }
        }
    }

    // compares two dates(value and target) with the operator
    private static boolean matches(LocalDate value, LocalDate target, Operations operator) {
        return switch (operator) {
            case EQUALS -> value.equals(target);
            case NOT_EQUALS -> !value.equals(target);
            case GREATER_THAN -> value.isAfter(target);
            case LESS_THAN -> value.isBefore(target);
            default -> throw new IllegalArgumentException(
                    "Invalid Operator " + operator + " for date fields");
        };
    }
}
