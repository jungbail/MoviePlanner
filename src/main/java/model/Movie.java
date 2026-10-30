package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * This represents a single movie in the application.
 * <p>
 * The record only includes the fields necessary for searching, sorting, filtering and display.
 * Unknown JSON fields are ignored during deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Movie(
        int id,
        String title,
        String overview,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("vote_average") double voteAverage,
        double popularity,
        @JsonProperty("genre_ids") List<Integer> genreIds,
        @JsonProperty("poster_path") String posterPath)
{}