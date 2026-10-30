package model;

import java.util.Set;

/**
 * Utility class for applying numeric filters to a collection of Movies
 */
public final class DoubleFilterHelper {

    /** Private constructor to prevent instantiation. */
    private DoubleFilterHelper() {}

    /**
     * Filters movies from the working list based on a numeric field and operator
     *
     * @param workingList source movie list
     * @param currentFilteredMovies target movie list
     * @param param the numeric value to filter on a string
     * @param operator comparison operator
     * @param field the movie field to filter on (vote average or popularity)
     */
    public static void doubleFilter(Set<Movie> workingList, Set<Movie> currentFilteredMovies,
                                    String param, Operations operator, String field) {
        double target = Double.parseDouble(param); // Sets a target
        for (Movie movie : workingList) { // For every movie in the working list
            double value = getFieldValue(movie, field); // Get the value were comparing to fall all the movies in the list
            if (matches(value, target, operator)) {
                currentFilteredMovies.add(movie); // Add to the current file any movies that match the filter
            }
        }
    }

    // Helper class helper method that calls getters depending on field and game
    private static double getFieldValue(Movie movie, String field) {
        return switch (field) {
            case "vote_average" -> movie.voteAverage();
            case "popularity" -> movie.popularity();
            default -> throw new IllegalArgumentException("Invalid double field " + field);
        };
    }

    // compares two numeric values with operation if found
    private static boolean matches(double value, double target, Operations operator) {
        return switch (operator) {
            case EQUALS -> value == target;
            case NOT_EQUALS -> value != target;
            case GREATER_THAN -> value > target;
            case LESS_THAN -> value < target;
            case GREATER_THAN_EQUALS -> value >= target;
            case LESS_THAN_EQUALS -> value <= target;
            default -> throw new IllegalArgumentException(
                    "Invalid Operator " + operator + " for double fields");
        };
    }
}
