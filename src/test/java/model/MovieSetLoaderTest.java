package model;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MovieSetLoader}.
 */
public class MovieSetLoaderTest {
    private final Set<Movie> movies = MovieSetLoader.loadMovies("data/movies.json");
    private List<Movie> movieList = new ArrayList<>(movies);



    @Test
    public void testLoadMovies() {
        Comparator<Movie> byTitleLettersThenOthers =
                Comparator.comparingInt((Movie m) -> {
                            String t = m.title() == null ? "" : m.title().trim();
                            return (!t.isEmpty() && Character.isLetter(t.charAt(0))) ? 0 : 1;
                        })
                        .thenComparing(m -> m.title().toLowerCase());
        movieList.sort(byTitleLettersThenOthers);

        assertEquals(50, movieList.size());
        assertEquals(348, movieList.get(0).id());
        assertEquals("Alien", movieList.get(0).title());
        assertEquals("1979-05-25", movieList.get(0).releaseDate());
        assertEquals(8.169, movieList.get(0).voteAverage(), 0.001);
        assertEquals(16.3593, movieList.get(0).popularity(), 0.001);
        assertEquals(List.of(27, 878), movieList.get(0).genreIds());
    }
}
