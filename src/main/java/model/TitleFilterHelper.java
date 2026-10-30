package model;

import java.util.Set;

/**
 * Utility class for applying title based filters to a collection of Movies
 */
public final class TitleFilterHelper {

    /** Private constructor to prevent instantiation. */
    private TitleFilterHelper() {}

    /**
     * Applies a title-based filter rule to the working list.
     *
     * @param workingList source movies to evaluate
     * @param currentFilteredMovies target set to collect matches
     * @param param value from the filter string, the search input
     * @param operator comparison operator
     */
    public static void titleFilter(Set<Movie> workingList, Set<Movie> currentFilteredMovies,
                                  String param, Operations operator) {
        String target = param.toLowerCase();
        for (Movie movie : workingList) {
            String value = movie.title().toLowerCase();
            if (titleMatches(value, target, operator)) {
                currentFilteredMovies.add(movie);
            }
        }
    }

    // checks the movie title with the user's search string(target) on the given operator
    private static boolean titleMatches(String value, String target, Operations operator) {
        return switch (operator) {
            case EQUALS -> value.equals(target);
            case NOT_EQUALS -> !value.equals(target);
            case CONTAINS -> value.contains(target);
            case GREATER_THAN -> value.compareTo(target) > 0;
            case LESS_THAN -> value.compareTo(target) < 0;
            case GREATER_THAN_EQUALS -> value.compareTo(target) >= 0;
            case LESS_THAN_EQUALS -> value.compareTo(target) <= 0;
            default -> false;
        };
    }
}
