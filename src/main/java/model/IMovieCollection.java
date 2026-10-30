package model;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This interface defines the core behavior for querying movies from within the application.
 */
public interface IMovieCollection {
    
    /**
     * Sorting and filter strings can be any of the following:
     * "title", "release_date", "vote_average", "runtime", "genre"
     * Filter the Movies by passing in filters
     * @param filterCriteria the filter criteria to apply to the movies.
     * @param sortCriteria the sort criteria to apply to the movies.
     * @param ascending whether to sort in ascending or descending order
     * @return a stream of movies that match the filter criteria.
     */
    Stream<Movie> filter(String filterCriteria, String sortCriteria, boolean ascending);
    
    
    
    /**
     * Searches for a movie by title in the in-memory collection only (loaded from data/movies.json
     * and any titles added earlier in this session).
     *
     * @param title the movie title (case-insensitive) to search for.
     * @return an Optional containing the Movie if found locally; else Optional.empty().
     */
    Optional<Movie> search(String title);

    /**
     * Fetches a movie from The Movie Database API by title using the given bearer token.
     * If a movie is returned, it is added to the in-memory collection for the session (not persisted to disk).
     *
     * @param title the movie title to search for.
     * @param bearerToken TMDB API read access token (Bearer).
     * @return an Optional containing the Movie if the API returns a result; else Optional.empty().
     */
    Optional<Movie> fetchFromTmdb(String title, String bearerToken);
}