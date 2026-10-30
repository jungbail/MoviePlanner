package controller;

import model.Movie;
import model.MovieCollection;
import model.Watchlist;
import view.IView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


/**
 * This class represents the Controller. The Controller handles view(GUI) events, delegates work to the model and
 * refreshes the view.
 */
public class MovieController implements ActionListener {
    /** Main movie collection used for search, filtering, sorting, and reset behavior. */
    private final MovieCollection model;
    /** User-selected subset of movies for review and export. */
    private final Watchlist watchlist;
    /** View contract used to read user input and update the state of the GUI. */
    private final IView view;
    /** Field used to sort movies before displaying them in the UI. */
    private String currentSortField = "title";
    /** Sort direction used when displaying movies in the UI. */
    private boolean ascending = true;


    /**
     * Constructs the controller and wires it to the view.
     * @param model the main movie collection model.
     * @param watchlist the watchlist model.
     * @param view the view contract used by the controller.
     */
    public MovieController(MovieCollection model, Watchlist watchlist, IView view) {
        this.model = model;
        this.watchlist = watchlist;
        this.view = view;
        this.view.setListener(this);
        refreshWorkingView();
        refreshWatchlistView();
    }

    /**
     * Handles action events fired by the GUI and routes them to the proper behavior.
     *
     * @param e the action event fired by the view.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        try {
            switch (cmd) {
                case "Filter Including Genre" -> {
                    model.filter("genre==" + view.getSelectedGenreName(), currentSortField, ascending)
                            .toList(); // force exec of stream
                    refreshWorkingView(); // pull updated data
                }
                case "Filter Excluding Genre" -> {
                    model.filter("genre!=" + view.getSelectedGenreName(), currentSortField, ascending)
                            .toList();
                    refreshWorkingView();
                }
                case "Popularity Greater Than" -> {
                    model.filter("popularity>" + view.getPopularity(), currentSortField, ascending)
                            .toList();
                    refreshWorkingView();
                }
                case "Popularity Lesser Than" -> {
                    model.filter("popularity<" + view.getPopularity(), currentSortField, ascending)
                            .toList();
                    refreshWorkingView();
                }
                case "Vote Greater Than" -> {
                    model.filter("vote_average>" + view.getVote(), currentSortField, ascending)
                            .toList();
                    refreshWorkingView();
                }
                case "Vote Lesser Than" -> {
                    model.filter("vote_average<" + view.getVote(), currentSortField, ascending).toList();
                    refreshWorkingView();
                }
                case "After Date" -> {
                    model.filter("release_date>" + view.getReleaseDate(), currentSortField, ascending)
                            .toList();
                    refreshWorkingView();
                }
                case "Before Date" -> {
                    model.filter("release_date<" + view.getReleaseDate(), currentSortField, ascending)
                            .toList();
                    refreshWorkingView();
                }
                case "Search Title" -> handleSearch();
                case "Add selected movies" -> handleAddSelected();
                case "Remove Selected Movies" -> handleRemoveSelected();
                case "Sort by Genre" -> {
                    currentSortField = "genre";
                    refreshBothViews();
                }
                case "Sort by Title" -> {
                    currentSortField = "title";
                    refreshBothViews();
                }
                case "Sort by Data" -> {
                    currentSortField = "release_date";
                    refreshBothViews();
                }
                case "Sort by Vote" -> {
                    currentSortField = "vote_average";
                    refreshBothViews();
                }
                case "Sort by Popularity" -> {
                    currentSortField = "popularity";
                    refreshBothViews();
                }
                case "Sort Ascending" -> {
                    ascending = true;
                    refreshBothViews();
                }
                case "Sort Descending" -> {
                    ascending = false;
                    refreshBothViews();
                }
                case "Reset Button" -> {
                    model.resetWorkingSet();
                    refreshWorkingView();
                }
                case "Export Button" -> handleExport();
                default -> {
                    // catch all for no ops
                }
            }
        } catch (NumberFormatException ex) {
            view.showError("Please enter a valid number.");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage()); // pass error to client
        } catch (Exception ex) {
            view.showError("Operation failed: " + ex.getMessage());
        }
    }


    /** Handles title search using local state, then optional TMDB lookup after token prompt. */
    private void handleSearch() {
        String title = view.getSearchTitle();

        // gate: validate title input
        if (title == null || title.isBlank()) {
            clearSearchResultDetails();
            view.showError("Please enter a movie title.");
            return;
        }

        Optional<Movie> local = model.search(title);
        if (local.isPresent()) {
            Movie movie = local.get();
            refreshWorkingView();
            setSearchResultDetails(movie);
            view.showInfo("Movie found: " + movie.title());
            return;
        }

        String token = view.promptForTmdbBearerToken();
        if (token == null) {
            clearSearchResultDetails();
            view.showInfo("Movie not found.");
            return;
        }
        if (token.isBlank()) {
            clearSearchResultDetails();
            view.showError("A TMDB bearer token is required to search the API.");
            return;
        }

        Optional<Movie> fromApi = model.fetchFromTmdb(title, token);
        if (fromApi.isPresent()) {
            Movie movie = fromApi.get();
            refreshWorkingView();
            setSearchResultDetails(movie);
            view.showInfo("Movie found: " + movie.title());
        } else {
            clearSearchResultDetails();
            view.showInfo("Movie not found.");
        }
    }

    private void setSearchResultDetails(Movie movie) {
        view.setSearchResultOverview(movie.overview());
        view.setSearchResultPopularity(movie.popularity());
        view.setSearchResultVoteAverage(movie.voteAverage());
        view.setSearchResultReleaseDate(movie.releaseDate());
        view.setPosterPath(movie.posterPath());
    }

    private void clearSearchResultDetails() {
        view.setSearchResultOverview(null);
        view.setSearchResultPopularity(Double.NaN);
        view.setSearchResultVoteAverage(Double.NaN);
        view.setSearchResultReleaseDate(null);
        view.setPosterPath(null);
    }

    /** Adds the selected movies from the working list to the watchlist. */
    private void handleAddSelected() {
        List<String> selectedTitles = view.getSelectedWorkingMovieTitles();
        for (String title : selectedTitles) {
            model.findMovieByTitleInWorkingSet(title).ifPresent(watchlist::add);
        }
        refreshWatchlistView();
    }

    /** Removes the selected movies from the watchlist. */
    private void handleRemoveSelected() {
        List<String> selectedTitles = view.getSelectedWatchlistMovieTitles();
        for (String title : selectedTitles) {
            watchlist.removeByTitle(title);
        }
        refreshWatchlistView();
    }

    /** Exports the watchlist to the user-provided file. */
    private void handleExport() throws IOException {
        String filename = view.getExportFile();
        if (filename == null || filename.isBlank()) {
            view.showError("Enter an export filename, e.g. watchlist.json");
            return;
        }
        watchlist.exportToJson(filename);
        view.showInfo("Watchlist exported to " + filename);
    }

    /** Refreshes the working movie list using the current sort state. */
    private void refreshWorkingView() {
        view.setWorkingMovies(model.getWorkingMoviesSorted(currentSortField, ascending));
    }

    /** Refreshes the watchlist using the current sort state. */
    private void refreshWatchlistView() {
        view.setWatchlistMovies(watchlist.getMoviesSorted(currentSortField, ascending));
    }

    /** Refreshes both movie list panels. */
    private void refreshBothViews() {
        refreshWorkingView();
        refreshWatchlistView();
    }
}