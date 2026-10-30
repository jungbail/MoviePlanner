package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the filter and compare helpers, run against the 50-movie catalog in data/movies.json.
 */
public class FilterHelperTests {

    private final Set<Movie> movies = MovieSetLoader.loadMovies("data/movies.json");

    private final Set<Movie> filteredMovies = new HashSet<>();

    @BeforeEach
    public void setupClear() {
        filteredMovies.clear();
    }

    // TESTING CompareHelper

    @Test
    public void testCompareHelperTitle() {
        Comparator<Movie> comp = CompareHelper.getComp("title", true);
        List<Movie> movieList = new ArrayList<>(movies);
        movieList.sort(comp);
        assertEquals("Alien", movieList.get(0).title());
        assertEquals("Avatar", movieList.get(1).title());
        assertEquals("Avengers: Endgame", movieList.get(2).title());
        assertEquals("Back to the Future", movieList.get(3).title());
    }

    @Test
    public void testCompareHelperReleaseDate() {
        Comparator<Movie> comp = CompareHelper.getComp("release_date", true);
        List<Movie> movieList = new ArrayList<>(movies);
        movieList.sort(comp);
        assertEquals("The Wizard of Oz", movieList.get(0).title());
        assertEquals("The Godfather", movieList.get(1).title());
        assertEquals("Jaws", movieList.get(2).title());
        assertEquals("Alien", movieList.get(3).title());
    }

    // TESTING GenreFilterHelper

    @Test
    public void testGenreGetter() {
        assertEquals("Action", GenreFilterHelper.getGenreName(28));
        assertEquals("Adventure", GenreFilterHelper.getGenreName(12));
        assertEquals("Science Fiction", GenreFilterHelper.getGenreName(878));
        assertEquals("Family", GenreFilterHelper.getGenreName(10751));
        assertEquals("Fantasy", GenreFilterHelper.getGenreName(14));
        assertEquals("History", GenreFilterHelper.getGenreName(36));
        assertEquals("Horror", GenreFilterHelper.getGenreName(27));
        assertEquals("Music", GenreFilterHelper.getGenreName(10402));
        assertEquals("Mystery", GenreFilterHelper.getGenreName(9648));
    }

    @Test
    public void testGenreFilterEquals() {
        GenreFilterHelper.genreFilter(movies, filteredMovies, "drama", Operations.EQUALS);
        assertEquals(20, filteredMovies.size());

        List<Movie> movieList = new ArrayList<>(filteredMovies);
        movieList.sort(CompareHelper.getComp("title", false));
        assertEquals("Whiplash", movieList.get(0).title());
        assertEquals("Top Gun: Maverick", movieList.get(1).title());
        assertEquals("Titanic", movieList.get(2).title());
    }

    @Test
    public void testGenreFilterRejectsContains() {
        assertThrows(IllegalArgumentException.class,
                () -> GenreFilterHelper.genreFilter(movies, filteredMovies, "dra", Operations.CONTAINS));
    }

    @Test
    public void testGenreFiltering() {
        GenreFilterHelper.genreFilter(movies, filteredMovies, "Action", Operations.EQUALS);
        // Narrow the Action set: exclude any movie that still has Drama (second pass must use prior result).
        GenreFilterHelper.genreFilter(filteredMovies, filteredMovies, "Drama", Operations.NOT_EQUALS);

        List<Movie> filteredMovieList = new ArrayList<>(filteredMovies);
        filteredMovieList.sort(CompareHelper.getComp("release_date", true));

        assertEquals("The Empire Strikes Back", filteredMovieList.get(0).title());
        assertEquals("The Terminator", filteredMovieList.get(1).title());
    }

    // TESTING DateFilterHelper

    @Test
    public void testDateFilterGreaterThan() {
        DateFilterHelper.dateFilter(movies, filteredMovies, "2020-03-01", Operations.GREATER_THAN);
        assertEquals(4, filteredMovies.size());
    }

    @Test
    public void testDateFilterLessThan() {
        DateFilterHelper.dateFilter(movies, filteredMovies, "1000-04-15", Operations.LESS_THAN);
        assertEquals(0, filteredMovies.size());
    }

    @Test
    public void testDateFilterLessThanAll() {
        DateFilterHelper.dateFilter(movies, filteredMovies, "3000-04-19", Operations.LESS_THAN);
        assertEquals(50, filteredMovies.size());
    }

    @Test
    public void testDateFilterEquals() {
        DateFilterHelper.dateFilter(movies, filteredMovies, "1985-07-03", Operations.EQUALS);
        assertEquals(1, filteredMovies.size());
        assertEquals("Back to the Future", filteredMovies.iterator().next().title());
    }

    @Test
    public void testDateFilterNotEquals() {
        DateFilterHelper.dateFilter(movies, filteredMovies, "1985-07-03", Operations.NOT_EQUALS);
        assertEquals(49, filteredMovies.size());
    }

    // TESTING DoubleFilterHelper

    @Test
    public void testDoubleFilterHelperVoteAvg() {
        DoubleFilterHelper.doubleFilter(movies, filteredMovies, "8.152", Operations.EQUALS, "vote_average");
        assertEquals(2, filteredMovies.size());

        List<Movie> movieList = new ArrayList<>(filteredMovies);
        movieList.sort(CompareHelper.getComp("title", true));
        assertEquals("The Truman Show", movieList.get(0).title());
        assertEquals("Top Gun: Maverick", movieList.get(1).title());
    }

    @Test
    public void testDoubleFilterHelperNotEquals() {
        DoubleFilterHelper.doubleFilter(movies, filteredMovies, "8.152", Operations.NOT_EQUALS, "vote_average");
        assertEquals(48, filteredMovies.size());
    }

    @Test
    public void testDoubleFilterHelperGreaterThan() {
        DoubleFilterHelper.doubleFilter(movies, filteredMovies, "30", Operations.GREATER_THAN, "popularity");
        assertEquals(9, filteredMovies.size());
    }

    @Test
    public void testDoubleFilterHelperLessThan() {
        DoubleFilterHelper.doubleFilter(movies, filteredMovies, "30", Operations.LESS_THAN, "popularity");
        assertEquals(41, filteredMovies.size());
    }

    @Test
    public void testDoubleFilterGreaterThanEquals() {
        DoubleFilterHelper.doubleFilter(movies, filteredMovies, "8.5", Operations.GREATER_THAN_EQUALS,
                "vote_average");
        assertEquals(6, filteredMovies.size());

        List<Movie> movieList = new ArrayList<>(filteredMovies);
        movieList.sort(CompareHelper.getComp("title", true));
        assertEquals("Schindler's List", movieList.get(0).title());
        assertEquals("The Shawshank Redemption", movieList.get(5).title());
    }

    @Test
    public void testDoubleFilterLessThanEquals() {
        DoubleFilterHelper.doubleFilter(movies, filteredMovies, "10", Operations.LESS_THAN_EQUALS, "popularity");
        assertEquals(2, filteredMovies.size());

        List<Movie> movieList = new ArrayList<>(filteredMovies);
        movieList.sort(CompareHelper.getComp("title", true));
        assertEquals("The Princess Bride", movieList.get(0).title());
        assertEquals("The Social Network", movieList.get(1).title());
    }

    // TESTING TitleFilterHelper

    @Test
    public void testTitleEquals() {
        TitleFilterHelper.titleFilter(movies, filteredMovies, "THE MATRIX", Operations.EQUALS);
        assertEquals(1, filteredMovies.size());
        assertEquals("The Matrix", filteredMovies.iterator().next().title());
    }

    @Test
    public void testTitleContains() {
        TitleFilterHelper.titleFilter(movies, filteredMovies, "harry potter", Operations.CONTAINS);
        assertEquals(1, filteredMovies.size());
        assertEquals("Harry Potter and the Philosopher's Stone", filteredMovies.iterator().next().title());
    }

    @Test
    public void testTitleNotEquals() {
        TitleFilterHelper.titleFilter(movies, filteredMovies, "the matrix", Operations.NOT_EQUALS);
        assertEquals(49, filteredMovies.size());
    }

    @Test
    public void testTitleGreaterThanEquals() {
        TitleFilterHelper.titleFilter(movies, filteredMovies, "top", Operations.GREATER_THAN_EQUALS);
        assertEquals(4, filteredMovies.size());

        List<Movie> movieList = new ArrayList<>(filteredMovies);
        movieList.sort(CompareHelper.getComp("title", true));
        assertEquals("Top Gun: Maverick", movieList.get(0).title());
        assertEquals("Whiplash", movieList.get(3).title());
    }

    @Test
    public void testTitleLessThan() {
        TitleFilterHelper.titleFilter(movies, filteredMovies, "top", Operations.LESS_THAN);
        assertEquals(46, filteredMovies.size());
    }
}
