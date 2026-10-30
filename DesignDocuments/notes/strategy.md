# <u>Strategy</u>

## High Level Design

---
### Components
1. Local database
   * data/movies.json
   * persistent on disk
   * startup seed data

2. In-memory master collection
   * all movies currently available in memory
   * starts from local database; can grow when API finds a missing movie
   * <b>new additions not automatically written back to disk</b>

3. Watchlist
    * user-selected subset from the In-memory master collection
    * <b>this</b> is what gets exported


---
### Flow
1. User starts the program which loads the local DB into MovieCollection state
2. User searches the in-memory collection for a movie (i.e. - title), which was loaded from the local DB.
   * If found, it returns it.
   * If not found, it makes an API call (delegate to helper) to fetch it from TMDB and adds it to state.
     * To facilitate browsing, user can sort and filter this master collection.
   * With each movie, the option to addToWatchlist should be present
3. Next, the user can add movies from state to a watchlist.
4. The user can then export movies from the watchlist to disk for persistence.

---
### Behavior Constraints
1. results panel = browse/search/filter/sort current in-memory collection
2. watchlist panel = chosen movies only
3. addToWatchlist = available on every visible result
4. MovieCollection - search(), sort() and filter() should apply to this to facilitate discovery.
   * this panel is used for discovery
6. Watchlist - User reviews what they've selected. This should support export() and sort().
   * this panel is used for review