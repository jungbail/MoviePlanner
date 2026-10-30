package model;

import java.util.Comparator;

/**
 * Utility class for comparing movies.
 */
public final class CompareHelper {

    /** Private constructor to prevent instantiation. */
    private CompareHelper() {}

    /**
     * Builds a comparator for Movie objects based on the given field and ascending flag.
     * @param field the field to compare on.
     * @param ascending whether to sort in ascending or descending order.
     * @return a comparator for Movie objects.
     */
    public static Comparator<Movie> getComp(String field, boolean ascending) {
        Comparator<Movie> comp = switch (field) {
            case "title" -> Comparator.comparing(Movie::title);
            case "release_date" -> Comparator.comparing(Movie::releaseDate);
            case "vote_average" -> Comparator.comparing(Movie::voteAverage);
            case "popularity" -> Comparator.comparing(Movie::popularity);

            case "genre" -> Comparator.comparing(movie -> {
                if (movie.genreIds() == null || movie.genreIds().isEmpty()) {
                    return "";
                }
                // use first genre id → map to name
                String name = GenreFilterHelper.getGenreName(movie.genreIds().get(0));
                return name == null ? "" : name;
            }, String.CASE_INSENSITIVE_ORDER);

            default -> throw new IllegalArgumentException("Invalid field: " + field);
        };

        if (!ascending) {
            comp = comp.reversed();
        }
        return comp;
    }
}
