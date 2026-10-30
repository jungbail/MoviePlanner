package view;

import model.Movie;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * This interface represents the contract for what the controller is allowed to ask the view to do.
 * It provides methods for getting raw user input, updating displayed data, accessing user selections and
 * showing feedback messages.
 */
public interface IView {

    /** Registers the controller (as an ActionListener) with the view to handle user interactions.  */
    void setListener(ActionListener actionEvent);

    /** Gets the genre the user selected from the genre dropdown. */
    String getSelectedGenreName();

    /** Gets the text the user typed into the search field. */
    String getSearchTitle();

    /**
     * Shows the movie overview under the search bar on the Search by Title tab.
     */
    void setSearchResultOverview(String overview);

    /**
     * Shows the movie popularity under the search bar on the Search by Title tab.
     */
    void setSearchResultPopularity(double popularity);

    /**
     * Shows the movie vote average under the search bar on the Search by Title tab.
     * Pass {@link Double#NaN} to clear.
     */
    void setSearchResultVoteAverage(double voteAverage);

    /**
     * Shows the movie release date under the search bar on the Search by Title tab.
     * Pass {@code null} or blank to clear.
     */
    void setSearchResultReleaseDate(String releaseDate);

    /** Sets the poster path for the movie image in search */
    void setPosterPath(String partialPath);

    /**
     * Prompts for a TMDB API bearer token (e.g. after a local title search misses).
     *
     * @return the trimmed token if the user confirms; {@code null} if the user cancels.
     */
    String promptForTmdbBearerToken();

    /** Gets the release date the user selected in the date dropdowns. */
    String getReleaseDate();

    /** Gets the value the user typed into the popularity field. */
    double getPopularity();

    /** Gets the value the user typed into the vote field. */
    double getVote();

    /** Gets the filename/path the user typed into the export field. */
    String getExportFile();

    /**
     * Populates the working movie list panel.
     * @param movies the movies to show in the working movie list.
     */
    void setWorkingMovies(List<Movie> movies);

    /**
     * Populates the watchlist panel.
     * @param movies the movies to show in the watchlist.
     */
    void setWatchlistMovies(List<Movie> movies);

    /**
     * Gets the titles currently selected in the working movie list panel.
     * Controller uses this to add movies from working list to watchlist (view -> controller).
     * @return the selected working movie list titles.
     */
    List<String> getSelectedWorkingMovieTitles();

    /**
     * Gets the titles currently selected in the watchlist panel.
     * Controller uses this to remove movies from watchlist.
     * @return the selected watchlist movie titles.
     */
    List<String> getSelectedWatchlistMovieTitles();

    /**
     * Shows an error message to the user.
     * @param message the message to show.
     */
    void showError(String message);

    /**
     * Shows an informational message to the user.
     * @param message the message to show.
     */
    void showInfo(String message);
}
