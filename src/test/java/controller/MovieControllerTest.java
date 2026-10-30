package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Movie;
import model.MovieCollection;
import model.Watchlist;
import view.IView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MovieController.
 */
public class MovieControllerTest {

    private Movie inception;
    private Movie interstellar;
    private StubView view; // fake implementation of IView
    private TestMovieCollection model;
    private Watchlist watchlist;
    private MovieController controller;

    @BeforeEach
    public void setUp() {
        this.inception = new Movie(
                27205,
                "Inception",
                "Overview...",
                "2010-07-15",
                8.4,
                28.6011,
                List.of(28, 878, 12),
                ""
        );
        this.interstellar = new Movie(
                157336,
                "Interstellar",
                "Overview...",
                "2014-11-05",
                8.47,
                65.7255,
                List.of(12, 18, 878),
                ""
        );
        this.view = new StubView(); // fresh view for each test
        this.model = new TestMovieCollection(Set.of(this.inception, this.interstellar)); // seed movies for tests
        this.watchlist = new Watchlist(); // empty for each test
        this.controller = new MovieController(this.model, this.watchlist, this.view); // construct controller
    }



    @Test
    public void testConstructorInitializesListsAndRegistersListener() {
        assertNotNull(this.view.listener);
        assertFalse(this.view.lastWorkingMovies.isEmpty());
        assertTrue(this.view.lastWatchlistMovies.isEmpty());
    }

    @Test
    public void testBlankSearchShowsError() {
        this.view.searchTitle = "";
        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Search Title")
        );

        assertEquals("Please enter a movie title.", this.view.lastErrorMessage);
        assertNull(this.view.lastSearchOverview);
        assertNull(this.view.lastSearchPopularity);
        assertNull(this.view.lastSearchVoteAverage);
        assertNull(this.view.lastSearchReleaseDate);
    }

    @Test
    public void testFoundSearchShowsInfo() {
        this.view.searchTitle = "Inception";

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Search Title")
        );

        assertEquals("Movie found: Inception", this.view.lastInfoMessage);
        assertEquals(List.of("Inception"), this.view.lastWorkingMovies);
        assertEquals("Overview...", this.view.lastSearchOverview);
        assertEquals("28.60", this.view.lastSearchPopularity);
        assertEquals("8.40", this.view.lastSearchVoteAverage);
        assertEquals("2010-07-15", this.view.lastSearchReleaseDate);
    }

    @Test
    public void testNotFoundSearchShowsInfoWhenTokenPromptCancelled() {
        this.view.searchTitle = "NoSuchMovieTitle";
        this.view.tmdbPromptResult = null;

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Search Title")
        );

        assertEquals("Movie not found.", this.view.lastInfoMessage);
        assertNull(this.view.lastSearchOverview);
        assertNull(this.view.lastSearchPopularity);
        assertNull(this.view.lastSearchVoteAverage);
        assertNull(this.view.lastSearchReleaseDate);
    }

    @Test
    public void testNotFoundSearchUsesApiWhenTokenProvided() {
        this.view.searchTitle = "SomeUnknownTitle";
        this.view.tmdbPromptResult = "test-token";
        this.model.fetchOverride = Optional.of(this.interstellar);

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Search Title")
        );

        assertEquals("Movie found: Interstellar", this.view.lastInfoMessage);
        assertEquals(List.of("Interstellar"), this.view.lastWorkingMovies);
        assertEquals("Overview...", this.view.lastSearchOverview);
        assertEquals("65.73", this.view.lastSearchPopularity);
        assertEquals("8.47", this.view.lastSearchVoteAverage);
        assertEquals("2014-11-05", this.view.lastSearchReleaseDate);
    }

    @Test
    public void testBlankTokenAfterPromptShowsError() {
        this.view.searchTitle = "Unknown";
        this.view.tmdbPromptResult = "   ";

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Search Title")
        );

        assertEquals("A TMDB bearer token is required to search the API.", this.view.lastErrorMessage);
        assertNull(this.view.lastSearchOverview);
        assertNull(this.view.lastSearchPopularity);
        assertNull(this.view.lastSearchVoteAverage);
        assertNull(this.view.lastSearchReleaseDate);
    }

    @Test
    public void testAddSelectedMoviesUpdatesWatchlist() {
        this.view.selectedWorkingTitles = List.of("Inception");

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Add selected movies")
        );

        assertEquals(List.of("Inception"), this.view.lastWatchlistMovies);
    }

    @Test
    public void testRemoveSelectedMoviesUpdatesWatchlist() {
        this.view.selectedWorkingTitles = List.of("Inception");

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Add selected movies")
        );

        assertEquals(List.of("Inception"), this.view.lastWatchlistMovies);

        this.view.selectedWatchlistTitles = List.of("Inception");

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Remove Selected Movies")
        );

        assertTrue(this.view.lastWatchlistMovies.isEmpty());
    }

    @Test
    public void testBlankExportFilenameShowsError() {
        this.view.exportFile = "";

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Export Button")
        );

        assertEquals("Enter an export filename, e.g. watchlist.json", this.view.lastErrorMessage);
    }

    @Test
    public void testInvalidVoteInputShowsNumericError() {
        this.view.throwNumberFormatForVote = true;

        this.controller.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Vote Greater Than")
        );

        assertEquals("Please enter a valid number.", this.view.lastErrorMessage);
    }

    /**
     * This is a fake implementation of IView to facilitate testing.
     */
    private static class StubView implements IView {
        private ActionListener listener;

        private String selectedGenreName = "Action";
        private String searchTitle = "";
        private String releaseDate = "2010-01-01";
        private double popularity = 10.0;
        private double vote = 8.0;
        private String exportFile = "";

        private boolean throwNumberFormatForVote = false;

        private List<String> selectedWorkingTitles = new ArrayList<>();
        private List<String> selectedWatchlistTitles = new ArrayList<>();

        private List<String> lastWorkingMovies = new ArrayList<>();
        private List<String> lastWatchlistMovies = new ArrayList<>();

        private String lastErrorMessage;
        private String lastInfoMessage;

        private String lastSearchOverview;
        private String lastSearchPopularity;
        private String lastSearchVoteAverage;
        private String lastSearchReleaseDate;

        @Override
        public void setListener(ActionListener actionEvent) {
            this.listener = actionEvent;
        }

        @Override
        public String getSelectedGenreName() {
            return this.selectedGenreName;
        }

        @Override
        public String getSearchTitle() {
            return this.searchTitle;
        }

        @Override
        public void setSearchResultOverview(String overview) {
            if (overview == null || overview.isBlank()) {
                this.lastSearchOverview = null;
            } else {
                this.lastSearchOverview = overview;
            }
        }
        @Override
        public void setSearchResultPopularity(double popularity) {
            if (Double.isNaN(popularity)) {
                this.lastSearchPopularity = null;
            } else {
                this.lastSearchPopularity = String.format("%.2f", popularity);
            }
        }

        @Override
        public void setSearchResultVoteAverage(double voteAverage) {
            if (Double.isNaN(voteAverage)) {
                this.lastSearchVoteAverage = null;
            } else {
                this.lastSearchVoteAverage = String.format("%.2f", voteAverage);
            }
        }

        @Override
        public void setSearchResultReleaseDate(String releaseDate) {
            if (releaseDate == null || releaseDate.isBlank()) {
                this.lastSearchReleaseDate = null;
            } else {
                this.lastSearchReleaseDate = releaseDate;
            }
        }

        @Override
        public void setPosterPath(String partialPath) {
            // no-op for controller tests (no Swing poster)
        }

        private String tmdbPromptResult = null;

        @Override
        public String promptForTmdbBearerToken() {
            return this.tmdbPromptResult;
        }

        @Override
        public String getReleaseDate() {
            return this.releaseDate;
        }

        @Override
        public double getPopularity() {
            return this.popularity;
        }

        @Override
        public double getVote() {
            if (this.throwNumberFormatForVote) {
                throw new NumberFormatException("bad vote");
            }
            return this.vote;
        }

        @Override
        public String getExportFile() {
            return this.exportFile;
        }

        @Override
        public void setWorkingMovies(List<Movie> movies) {
            this.lastWorkingMovies = movies.stream().map(Movie::title).toList();
        }

        @Override
        public void setWatchlistMovies(List<Movie> movies) {
            this.lastWatchlistMovies = movies.stream().map(Movie::title).toList();
        }

        @Override
        public List<String> getSelectedWorkingMovieTitles() {
            return this.selectedWorkingTitles;
        }

        @Override
        public List<String> getSelectedWatchlistMovieTitles() {
            return this.selectedWatchlistTitles;
        }

        @Override
        public void showError(String message) {
            this.lastErrorMessage = message;
        }

        @Override
        public void showInfo(String message) {
            this.lastInfoMessage = message;
        }
    }

    /**
     * Private MovieCollection for controller tests.
     * This avoids depending on data/movies.json or a TMDB token.
     */
    private static class TestMovieCollection extends MovieCollection {
        private final List<Movie> allMovies;
        private List<Movie> workingMovies;
        /** When non-empty, {@link #fetchFromTmdb} returns this and updates the working list (for controller tests). */
        Optional<Movie> fetchOverride = Optional.empty();

        public TestMovieCollection(Set<Movie> movies) {
            super(movies, null);
            this.allMovies = new ArrayList<>(movies);
            this.workingMovies = new ArrayList<>(movies);
        }

        @Override
        public Optional<Movie> search(String title) {
            for (Movie movie : this.allMovies) {
                if (movie.title().equalsIgnoreCase(title)) {
                    this.workingMovies = List.of(movie);
                    return Optional.of(movie);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Movie> fetchFromTmdb(String title, String bearerToken) {
            if (this.fetchOverride.isEmpty()) {
                return Optional.empty();
            }
            Movie m = this.fetchOverride.get();
            this.workingMovies = List.of(m);
            return Optional.of(m);
        }

        @Override
        public List<Movie> getWorkingMoviesSorted(String sortCriteria, boolean ascending) {
            return this.workingMovies;
        }

        @Override
        public Optional<Movie> findMovieByTitleInWorkingSet(String title) {
            return this.workingMovies.stream()
                    .filter(m -> m.title().equalsIgnoreCase(title))
                    .findFirst();
        }

        @Override
        public void resetWorkingSet() {
            this.workingMovies = new ArrayList<>(this.allMovies);
        }
    }
}