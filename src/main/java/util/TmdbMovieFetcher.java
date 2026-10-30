package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * This class represents a utility script for seeding a local database (JSON file) with movies pulled down from TMDB.
 * Environment variable TMDB_BEARER_TOKEN must be set.
 * Client must provide their own token.
 */
public class TmdbMovieFetcher {
    // This is the name of the environment variable where the token should be found.
    private static final String BEARER_ENV_VAR = "TMDB_BEARER_TOKEN";
    // The base URL for the API endpoint.
    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    // This is Jackson's JSON mapper for parsing JSON and ultimately writing it to a file.
    private static final ObjectMapper MAPPER = new ObjectMapper();


    public static void main(String[] args) throws IOException, InterruptedException {
        // The number of movies to fetch from TMDB.
        int targetCount = 100; // default number of movies to fetch

        // use arg passed to main method to determine how many records to fetch
        if (args.length > 0) {
            try {
                targetCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("First argument must be an integer (targetCount)");
            }
        }

        // The Path to save movies to.
        Path outputFile = Path.of("data/movies.json");

        downloadMovies(targetCount, outputFile);
        System.out.println("Done. Movies saved to: " + outputFile.toAbsolutePath());
    }

    public static void downloadMovies(int targetCount, Path outputFile)
            throws IOException, InterruptedException {

        // gate: validate targetCount
        if (targetCount < 1) {
            throw new IllegalArgumentException("targetCount must be >= 1");
        }

        String bearerToken = System.getenv(BEARER_ENV_VAR); // get the bearer token from env

        // gate: validate env var is set
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + BEARER_ENV_VAR);
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        ArrayNode allMovies = MAPPER.createArrayNode(); // array placeholder to hold movies

        int page = 1; // start at page 1
        int totalPages = Integer.MAX_VALUE; // unbounded total to init loop

        while (allMovies.size() < targetCount && page <= totalPages) {
            // build URL; insert URL params
            String url = BASE_URL
                    + "?include_adult=false"
                    + "&include_video=false"
                    + "&language=en-US"
                    + "&sort_by=popularity.desc" // return the most popular movies first
                    + "&page=" + page;

            // build request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer " + bearerToken)
                    .GET()
                    .build();

            // send request
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()); // response returned as string

            // gate: unsuccessful HTTP request throws
            if (response.statusCode() != 200) {
                throw new IOException("Request failed. HTTP " + response.statusCode() + "\nBody:\n" + response.body());
            }

            JsonNode root = MAPPER.readTree(response.body()); // map response to JSON tree

            // validate and extract total_pages
            JsonNode totalPagesNode = root.get("total_pages");
            if (totalPagesNode == null || !totalPagesNode.isInt()) {
                throw new IOException("Invalid or missing total_pages");
            }
            totalPages = totalPagesNode.asInt();

            JsonNode results = root.path("results"); // read results
            // validate results
            if (!results.isArray()) {
                throw new IOException("Invalid or missing results");
            }

            // add each movie in results in current page to allMovies
            // skips movies with empty string for release_date
            for (JsonNode movie : results) {
                String releaseDate = movie.path("release_date").asText("");
                if (releaseDate == null || releaseDate.isBlank()){ // skips
                    continue;
                }
                allMovies.add(movie);
                if (allMovies.size() >= targetCount) {
                    break;
                }
            }

            System.out.println("Fetched page " + page
                    + ", total movies collected: " + allMovies.size());

            page++;
        }

        // write results to disk
        Files.writeString(
                outputFile,
                MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(allMovies)
        );
    }
}