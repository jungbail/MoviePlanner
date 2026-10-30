# Surface-Level UML (Initial)

This is a high-level UML draft for the `cs5004_final_project`, using the same overall shape as `BoardGameArenaPlanner` (launcher, app/controller, interfaces, implementations, model, loader/saver).

```mermaid
classDiagram
direction LR

class MovieCollectionApp {
  -MovieCollectionApp()  
  +main(args: String[]) void
}

class MovieController {
    -IMovieModel : model
    +MovieController()
    +run(args: String) : void
}

%% Filtering and sorting behavior
%% reset() - removes all filters and sorts.
class IPlannerModel {
    <<interface>>
    +filter(movieCollection: ~SetMovie~, query: String) : Stream~Movie~
    +sort(by: movieField) : Stream~Movie~
    +sort(by: movieField, ascending: boolean) : Stream~Movie~
    +reset() : void 
}

class PlannerModel {
    +filter(movieCollection: ~SetMovie~, query: String) : Stream~Movie~
    +sort(by: movieField) : Stream~Movie~
    +sort(by: movieField, ascending: boolean) : Stream~Movie~
    +reset() : void
}

%% User selected movies / user behavior
%% clear() - clears the list / resets the object's internal state
class IMovieListModel {
    <<interface>>
    +addToList(input: String, filtered: Stream~Movie~) : void
    +removeFromList(input: String) : void
    +saveMovies(filename: String) : void
    +getMovieNames() List~String~
    +clear() void
}

class MovieListModel {
  -selected: Set~Movie~
  +addToList(input: String, filtered: Stream~Movie~) : void
  +removeFromList(input: String) : void
  +getMovieNames() : Set~String~
  +saveMovies(filename: String) : void
  +clear() : void
}

%% ensure int can cover the movie ID range
%% capture genres from https://developer.themoviedb.org/reference/genre-movie-list, use SWITCH statement
%% We will capture the entire date and then we can filter by it accordingly
class Movie {
    -id: int
    -title: String
    -original_language: String
    -release_date: String
    -poster_path : String
    -originCountry: String
    -genres: List~String~
    -runtimeMin: int
    -vote_average: double
    +Movie(id, title, original_language, release_date, poster_path, originCountry, genres, runtimeMin, vote_average)

    + getId() int
    + getTitle() String
    + getOriginalLanguage() String
    + getReleaseDate() String
    + getPosterPath() String
    + getOriginCountry() String
    + getGenres() List
    + getRuntimeMin() int
    + getVoteAverage() double
}

class MovieField {
  <<enumeration>>
  TITLE
  YEAR
  RATING
  RUNTIME
  GENRE
}

class MoviesLoader {
  +loadFromApi(endpoint: String) Set~Movie~
  +loadFromFile(path: String) Set~Movie~
}

class WatchlistSaver {
  +saveAsJson(path: String, movies: List~Movie~) void
  +saveAsCsv(path: String, movies: List~Movie~) void
  +saveAsXml(path: String, movies: List~Movie~) void
}



MovieCollectionApp --> ConsoleApp : creates
MovieCollectionApp --> Planner : creates
MovieCollectionApp --> MovieList : creates
MovieCollectionApp --> MoviesLoader : uses

ConsoleApp --> IPlanner : uses
ConsoleApp --> IMovieList : uses

Planner ..|> IPlanner
MovieList ..|> IMovieList

Planner --> Movie : filters/sorts
MovieList --> Movie : stores
Planner --> MovieField : sort criteria

WatchlistSaver --> Movie : serializes
MovieList --> WatchlistSaver : delegates save
MoviesLoader --> Movie : builds
```


