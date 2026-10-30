package model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This class represents and manages a user's list of movies
 */
public class Watchlist {
    private final Set<Movie> selected = new LinkedHashSet<>();

    /**
     * Adds a movie to the watchlist
     * @param movie the movie to add
     */
    public void add(Movie movie) {
        if (movie != null) {
            selected.add(movie);
        }
    }

    /**
     * Adds all of the movies from a list to the watchlist
     * @param movies list of movies being added
     */
    public void addAll(List<Movie> movies) {
        if (movies != null) {
            selected.addAll(movies);
        }
    }

    /**
     * Removes a specific movie found in watchlist by title
     * @param title the title of the movie to remove
     */
    public void removeByTitle(String title) {
        selected.removeIf(m -> m.title().equalsIgnoreCase(title));
    }

    /**
     * Clears the watchlist from all movies
     */
    public void clear() {
        selected.clear();
    }

    /**
     * Returns all movies currently in the watchlist
     * @return a lit of movies
     */
    public List<Movie> getMovies() {
        return new ArrayList<>(selected);
    }

    /**
     * This returns the watchlist sorted by a given field and order direction
     * @param field the movie field to act on
     * @param ascending T/F if list will be in ascending or descending order
     * @return returns a list of sorted movies based on field provided and direction
     */
    public List<Movie> getMoviesSorted(String field, boolean ascending) {
        Comparator<Movie> comp = CompareHelper.getComp(field, ascending);
        return selected.stream().sorted(comp).toList();
    }

    /**
     * This checks if the watchlist has any movies
     * @return returns a T if watchlist is empty, F if not
     */
    public boolean isEmpty() {
        return selected.isEmpty();
    }

    /**
     * This expors the current watchlist to a json file
     * @param filename the name of output file
     * @throws IllegalArgumentException if the file name is empty
     * @throws IOException if the file can't be written
     */
    public void exportToJson(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be blank");
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), getMovies());
    }
}