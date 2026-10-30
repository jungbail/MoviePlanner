package model;

import data.TmdbMovieSearcher;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * In-memory master collection for the current session.
 */
public class MovieCollection implements IMovieCollection {
    private final Set<Movie> movies;
    private final Set<Movie> filteredMovies;
    private Set<Movie> workingSet;
    private final TmdbMovieSearcher movieSearcher;

    /**
     * Constructor for the MovieCollection for movie set
     */
    /**
     * Constructor for the MovieCollection.
     */
    public MovieCollection() {
        this(MovieSetLoader.loadMovies("data/movies.json"), new TmdbMovieSearcher());
    }

    /**
     * Constructor with a set movie-set and searcher if needed for making api call
     *
     * @param movies already constructed movie set
     * @param movieSearcher TMDB movie searcher that can fetch data from the movie api
     */
    public MovieCollection(Set<Movie> movies, TmdbMovieSearcher movieSearcher) {
        if (movies == null) {
            throw new IllegalArgumentException("movies cannot be null");
        }
        this.movies = new HashSet<>(movies);
        this.filteredMovies = new HashSet<>();
        this.workingSet = new HashSet<>(this.movies);
        this.movieSearcher = movieSearcher;
    }


    /**
     * Filters and sorts the movie set on given criteria, including order
     *
     * @param filterCriteria the filter criteria to apply to the movies
     * @param sortCriteria the sort criteria to apply to the movies
     * @param ascending T/F to sort in ascending or descending order
     * @return
     */
    @Override
    public Stream<Movie> filter(String filterCriteria, String sortCriteria, boolean ascending) {
        String normalized = (filterCriteria == null) ? "" : filterCriteria.trim();

        if (normalized.isEmpty()) {
            Comparator<Movie> comp = CompareHelper.getComp(sortCriteria, ascending);
            return workingSet.stream().sorted(comp);
        }

        // filters to be applied, split by comma
        String[] filteredList = normalized.contains(",")
                ? normalized.split("\\s*,\\s*")
                : new String[]{normalized};

        for (String filter : filteredList) {
            try {
                Operations operation = Operations.getOperationFromStr(filter);
                String operatorText = operation.getOperation();
                int operatorIndex = filter.indexOf(operatorText);

                String paramField = filter.substring(0, operatorIndex).trim();
                String paramValue = filter.substring(operatorIndex + operatorText.length()).trim();

                if (paramField.isEmpty() || paramValue.isEmpty()) {
                    throw new IllegalArgumentException("Invalid Filter " + filter);
                }

                switch (paramField) {
                    case "title" ->
                            TitleFilterHelper.titleFilter(workingSet, filteredMovies, paramValue, operation);
                    case "release_date" ->
                            DateFilterHelper.dateFilter(workingSet, filteredMovies, paramValue, operation);
                    case "vote_average", "popularity" ->
                            DoubleFilterHelper.doubleFilter(workingSet, filteredMovies, paramValue, operation, paramField);
                    case "genre" ->
                            GenreFilterHelper.genreFilter(workingSet, filteredMovies, paramValue, operation);
                    default -> throw new IllegalArgumentException("Invalid Parameter Field " + paramField);
                }

                workingSet.clear();
                workingSet.addAll(filteredMovies);
                filteredMovies.clear();

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Filter " + filter, e);
            }
        }

        Comparator<Movie> comp = CompareHelper.getComp(sortCriteria, ascending);
        return workingSet.stream().sorted(comp);
    }

    /**
     * Searches the in-memory movie collection for a movie by title.
     * Search is not case-sensitive
     *
     * @param title the movie title to search for.
     * @return an Optional containing the Movie if found; else Optional.empty().
     */
    @Override
    public Optional<Movie> search(String title) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        for (Movie movie : this.movies) {
            if (movie.title().equalsIgnoreCase(title.trim())) {
                workingSet.clear();
                workingSet.add(movie);
                return Optional.of(movie);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Movie> fetchFromTmdb(String title, String bearerToken) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }
        if (this.movieSearcher == null) {
            return Optional.empty();
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("TMDB bearer token is required for API search.");
        }

        Optional<Movie> fetchedMovie = this.movieSearcher.searchByTitle(title.trim(), bearerToken);
        fetchedMovie.ifPresent(movie -> {
            this.movies.add(movie);
            this.workingSet.clear();
            this.workingSet.add(movie);
        });

        return fetchedMovie;
    }

    public void resetWorkingSet() {
        this.workingSet = new HashSet<>(this.movies);
        this.filteredMovies.clear();
    }

    public List<Movie> getWorkingMoviesSorted(String sortCriteria, boolean ascending) {
        Comparator<Movie> comp = CompareHelper.getComp(sortCriteria, ascending);
        return this.workingSet.stream().sorted(comp).toList();
    }

    public List<Movie> getAllMoviesSorted(String sortCriteria, boolean ascending) {
        Comparator<Movie> comp = CompareHelper.getComp(sortCriteria, ascending);
        return this.movies.stream().sorted(comp).toList();
    }

    public Optional<Movie> findMovieByTitleInWorkingSet(String title) {
        return this.workingSet.stream()
                .filter(m -> m.title().equalsIgnoreCase(title))
                .findFirst();
    }

    public Optional<Movie> findMovieByTitleAnywhere(String title) {
        return this.movies.stream()
                .filter(m -> m.title().equalsIgnoreCase(title))
                .findFirst();
    }
}