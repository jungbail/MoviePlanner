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
 * This class is just like TmdbMovieFetcher but instead of importing the movie data it imports a JSON file of the genre code
 * and genre value pairs. This is much simpilar than the TmdbMovieFetcher because this should really only need to be run once
 */

public class TmdbGenreFetcher {

    private static final String BEARER_ENV_VER = "used key here";

    private static final String BASE_URL = "https://api.themoviedb.org/3/genre/movie/list?language=en";

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {

        Path outputFile = Path.of("data/genre.json");

        HttpClient client = HttpClient.newBuilder().build();

        ArrayNode genres = MAPPER.createArrayNode();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .timeout(Duration.ofSeconds(30))
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + BEARER_ENV_VER)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode results = root. path("genres");

        for (JsonNode genre : results) {
            genres.add(genre);
        }

        Files.writeString(outputFile, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(genres));

    }
}