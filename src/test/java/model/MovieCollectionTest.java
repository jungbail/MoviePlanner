package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import java.util.*;
import java.util.stream.Collectors;

import data.TmdbMovieSearcher;

/**
* Tests for MovieCollection.
*/
public class MovieCollectionTest {

    @Test
    public void testTitleMatch() {
        MovieCollection filteredMovies = new MovieCollection();
        List<Movie> listOfMovies = filteredMovies
        .filter("title~=Ac", "title", true)
        .collect(Collectors.toList());
        
        assertEquals(2, listOfMovies.size());
        assertEquals("Back to the Future", listOfMovies.get(0).title());
    }

    @Test
    public void testCompoundFiltering() {
        MovieCollection filteredMovies = new MovieCollection();
        List<Movie> listOfMovies = filteredMovies
                .filter("vote_average>=8, release_date>2000-10-01", "title", false)
                .collect(Collectors.toList());
        assertEquals(12, listOfMovies.size());
        assertEquals("Whiplash", listOfMovies.get(0).title());
    }

    @Test
    public void testCompoundGenreFiltering() {
        MovieCollection filteredMovies = new MovieCollection();
        List<Movie> listOfMovies = filteredMovies
                .filter("genre==action, genre!=drama", "title", false)
                .collect(Collectors.toList());
        assertEquals(10, listOfMovies.size());
    }


    private Movie inception;
    private MovieCollection collection;

    /**
     * Sets up shared test data before each test runs.
     */
    @BeforeEach
    public void setUp() {
        this.inception = new Movie(
                27205,
                "Inception",
                "Cobb, a skilled thief who commits corporate espionage by infiltrating the subconscious of his targets is offered a chance to regain his old life as payment for a task considered to be impossible: \"inception\", the implantation of another person's idea into a target's subconscious.",
                "2010-07-15",
                8.4,
                28.6011,
                List.of(28, 878, 12),
                ""
        );
        this.collection = new MovieCollection(Set.of(this.inception), null); // set with movie=Inception
    }

    /**
     * Tests that search returns a movie when the title exists in memory.
     */
    @Test
    public void testSearchReturnsMovieWhenFoundLocally() {
        Optional<Movie> result = this.collection.search("Inception");

        assertTrue(result.isPresent()); // got a result?
        assertEquals(this.inception, result.get()); // result is the movie, Inception?
    }

    /**
     * Tests that search is case-insensitive.
     */
    @Test
    public void testSearchIsCaseInsensitive() {
        Optional<Movie> result = this.collection.search("iNcEpTiOn");

        assertTrue(result.isPresent()); // got a result?
        assertEquals(this.inception, result.get()); // result is the movie, Inception?
    }

    /**
     * Tests that search returns an empty Optional when the movie does not exist in memory.
     */
    @Test
    public void testSearchReturnsEmptyWhenMovieNotFound() {
        Optional<Movie> result = this.collection.search("hn38hg834hi4jfgk");
        assertTrue(result.isEmpty());
    }

    @Disabled("Requires TMDB_BEARER_TOKEN and makes real API call")
    /**
     * NOTE:
     * This test makes a real call to the TMDB API.
     * It requires the environment variable TMDB_BEARER_TOKEN to be set.
     *
     * This test is disabled by default to avoid external dependencies in unit tests.
     */
//    @Test
    public void testSearchFallsBackToApiWhenMovieMissingLocally() {
        Set<Movie> emptyMovieSet = Set.of(); // create empty movie set, guarantees "Inception" not present

        TmdbMovieSearcher searcher = new TmdbMovieSearcher();
        MovieCollection collection = new MovieCollection(emptyMovieSet, searcher);

        Optional<Movie> result = collection.fetchFromTmdb("Inception", System.getenv("TMDB_BEARER_TOKEN"));

        assertTrue(result.isPresent()); // api fallback found a movie
        assertEquals("Inception", result.get().title()); // found movie is "Inception"
    }





    /**
     * Fake TMDB searcher for unit tests.
     * Avoids the need for a bearer token to be set.
     */
    private static class FakeMovieSearcher extends TmdbMovieSearcher {
        @Override
        public Optional<Movie> searchByTitle(String title, String bearerTokenOverride) {
            if (title.equalsIgnoreCase("Inception")) {
                return Optional.of(new Movie( // return a hardcoded, fake movie vs seeking it out via API call
                        27205,
                        "Inception",
                        "fake",
                        "2010-07-15",
                        8.4,
                        28.6,
                        List.of(),
                        ""
                ));
            }
            return Optional.empty();
        }
    }

    @Test
    public void testSearchFallsBackToFakeWhenMovieMissingLocally() {
        Set<Movie> emptyMovieSet = Set.of(); // guarantees we hit fallback logic

        TmdbMovieSearcher searcher = new FakeMovieSearcher(); // uses fake fallback result
        MovieCollection collection = new MovieCollection(emptyMovieSet, searcher);

        Optional<Movie> result = collection.fetchFromTmdb("Inception", "dummy-token");

        assertTrue(result.isPresent()); // got the result using the fallback?
        assertEquals("Inception", result.get().title()); // result was the hardcoded value?
    }
}