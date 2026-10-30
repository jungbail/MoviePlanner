package data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Movie;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Searches The Movie Database (TMDB) for a movie by title using the /search/movie endpoint.
 */
public class TmdbMovieSearcher {
    // Name of the environment variable that stores the TMDB bearer token.
    private static final String BEARER_ENV_VAR = "TMDB_BEARER_TOKEN";
    // Base URL for TMDB movie title search.
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    // Shared Jackson mapper used to parse JSON responses from TMDB.
    private static final ObjectMapper MAPPER = new ObjectMapper();
    // HTTP client used to send search requests to TMDB.
    private final HttpClient client;

    /**
     * Constructs a TMDB movie searcher with a configured HTTP client.
     */
    public TmdbMovieSearcher() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    /**
     * Searches TMDB for a movie by title using {@code TMDB_BEARER_TOKEN} from the environment if set.
     *
     * @param title the movie title to search for.
     * @return an Optional containing the first matching Movie, or Optional.empty() if no result exists.
     * @throws IllegalArgumentException if title is null or blank.
     * @throws IllegalStateException if the bearer token is missing or the API call fails.
     */
    public Optional<Movie> searchByTitle(String title) {
        return searchByTitle(title, null);
    }

    /**
     * Searches TMDB for a movie by title.
     * Uses the non-blank {@code bearerTokenOverride} when provided; otherwise falls back to {@code TMDB_BEARER_TOKEN}.
     *
     * @param title the movie title to search for.
     * @param bearerTokenOverride optional token from the user (e.g. dialog); may be null to use only the env var.
     * @return an Optional containing the first matching Movie, or Optional.empty() if no result exists.
     */
    public Optional<Movie> searchByTitle(String title, String bearerTokenOverride) {
        // gate: reject invalid title input before making any API call
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }

        String bearerToken = (bearerTokenOverride != null && !bearerTokenOverride.isBlank())
                ? bearerTokenOverride.trim()
                : System.getenv(BEARER_ENV_VAR);
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalStateException(
                    "Missing TMDB bearer token. Set " + BEARER_ENV_VAR + " or provide a token when searching.");
        }

        // provides URL safety for title (e.g. - spaces, special chars, etc.)
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        // Build TMDB search URL with query params for title, language, and first page of results.
        String url = SEARCH_URL
                + "?query=" + encodedTitle
                + "&include_adult=false"
                + "&language=en-US"
                + "&page=1";

        // Build authenticated GET request for the TMDB search endpoint.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + bearerToken) // send TMDB read access token
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    this.client.send(request, HttpResponse.BodyHandlers.ofString()); // Send request; get response

            // gate: fail fast if TMDB did not return a successful response
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "TMDB search request failed. HTTP " + response.statusCode()
                                + "\nBody:\n" + response.body()
                );
            }

            // Parse the JSON response and extract the JSON array of results
            JsonNode root = MAPPER.readTree(response.body()); // parse response into JSON tree
            JsonNode results = root.path("results"); // focus on results field

            // title not found is valid outcome
            if (!results.isArray() || results.isEmpty()) {
                return Optional.empty();
            }

            // Use the top-ranked TMDB result as the best match for now.
            JsonNode firstResult = results.get(0); // get top result
            return Optional.of(this.mapToMovie(firstResult)); // map to movie record

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read or parse TMDB response.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("TMDB search request was interrupted.", e);
        }
    }

    /**
     * Maps one TMDB result object into the application's Movie record.
     *
     * @param node one JSON object from TMDB's results JSON array.
     * @return the mapped Movie.
     */
    private Movie mapToMovie(JsonNode node) {
        int id = node.path("id").asInt();
        String title = node.path("title").asText("");
        String overview = node.path("overview").asText("");
        String releaseDate = node.path("release_date").asText("");
        double voteAverage = node.path("vote_average").asDouble(0.0);
        double popularity = node.path("popularity").asDouble(0.0);
        String posterPath = node.path("poster_path").asText("");

        // Convert TMDB genre_ids array into the List<Integer> expected by Movie.
        List<Integer> genreIds = new ArrayList<>();
        JsonNode genreIdsNode = node.path("genre_ids");
        if (genreIdsNode.isArray()) {
            for (JsonNode genreId : genreIdsNode) {
                genreIds.add(genreId.asInt());
            }
        }
        // Build and return the application's Movie record from the mapped fields.
        return new Movie(
                id,
                title,
                overview,
                releaseDate,
                voteAverage,
                popularity,
                genreIds,
                posterPath
        );
    }
}