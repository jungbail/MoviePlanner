## Product Proposal - Movie Collection Planner

### 1. What are you building?

We are building a Java Swing application that manages a collection of movies and allows users to explore, filter and organize them into custom watchlists.

The application will:
* Display a collection of movies
* Allow users to filter and sort movies based on criteria (genre, rating, year, etc.)
* Enable users to create and manage custom watch lists
* Allow exporting those lists to file formats such as JSON or CSV

The application will follow OOD principles and align with MVC architecture to separate concerns.
We will unit test model and controller behavior with JUnit. 
We will also maintain Javadoc comments, update UML as the design evolves, and keep a README/manual that explains how to run and use the application.

### 2. What are the initial (REQUIRED) features for the application? 

<mark><b>We need all of the required features + 3 more to exceed expectations.</b></mark>

1. Graphical User Interface (using java swing)
   1. This will be responsible for displaying a collection of movies.
   2. It will have panels for watchlist management (e.g. - filter, sort, reset)
2. Be able to view all items in the collection - in a logical order
   1. Display all movies in a logical order (e.g. - lexicographical, by rating, etc.)
3. Be able to build a list of items from the collection according to different criteria
   1. Users can build watchlists using filters for at least *Genre* and *Rating*
4. Be able to save out that list using a file format we covered in the course such as .xml, .json, or .csv.
   1. Users can export watchlists to JSON (default) or CSV (optional)

### 3. What are the minimum additional features you plan to implement?

To meet project expectations (based on group size requirements), we will implement the following.
1. Be able to search for items in the collection --> Search movies by title (substring match)
2. Be able to sort items in the collection --> Sort movies by Title, Year, Rating
3. Be able to filter items in the collection --> Filter by Genre, Rating, Year

### 4. What are your stretch goals (features beyond the minimum)?

1. API Integration - Fetch movie data from an external API (e.g. - [TMDB](https://developer.themoviedb.org/reference/getting-started))
2. Movie Image Display - Show movie posters in GUI
3. Extend the application to support TB shows as an additional media type. ref: open closed principle

### 5. Initial Design (Special emphasis should be placed on how you plan to break it up)

We will follow MVC to ensure separation of concerns and keep the code clean. This separation improves maintainability, testability, and allows team members to work on different components in parallel.
and make code maintenance (e.g. - changes) more difficult.

<mark>Model - Implements filtering, sorting, searching and list-building logic.</mark>
* Movie
* MovieCollection
* Watchlist
* MovieFilter
* MovieSorter

<mark>Model - DataLayer</mark>
* MovieData - loads movie data (API or local)
* WatchListFile - saves/loads watchlists

Use interfaces around data access so that file and API sources can be used interchangeably without having to modify the rest of the application.

<mark>Controller - bridge between view and model. Receives user input from GUI, delegates operations to model, updates view with results.</mark>
* Decides when to filter or sort and how --> Model contains the actual Filter and Sort logic
* If user clicks filter, this will inform the model and update view.

<mark>View - User friendly interface for the user using Swing</mark>
Responsible for showing panels/buttons, search box and results display. It will not contain business logic; all processing is handled by the controller and model.
* Main Frame
* Panels (e.g. - filter, sort, view current list, save, reset/clear)

## Initial UML Design (High-Level)
MovieCollection contains Movie
Watchlist -> contains Movie

### 6. How do you plan to break up the work?

We will follow a feature-based development approach using GitHub:
Workflow
1. Create feature branch
2. Implement feature
3. Submit pull request
4. Team review // facilitates learning and refinement
5. Merge to main // this is our equivalent of "production code"

This follows GitHub practices for collaborate development.
The repo is backed up periodically with history for disaster recovery.
Team is up to speed on our branch strategy.

<u>Work Distribution</u>
<b>Developer</b>
Model classes (MovieCollection, Movie, Watchlist)
<b>Developer</b>
Filtering and sorting logic
<b>Developer</b>
File handling (save)
<b>Developer</b>
API integration (if implemented)
<b>Developer</b>
* Get collection
<b>Developer</b>
* Get movie posters
<b>Developer || Everyone</b>
GUI implementation (Swing)
* Controller integration
<b>Everyone</b>
* Junit testing
* Documentation (Javadoc, README)
* UML updates
* Code Reviews

### 7. What is your teams timeline and major check-in points?
Roadmap
Week 1 - Plan/Setup
* Finalize idea and scope
* Create GitHub repo + add team & professor/TA as collaborators - DONE
* Initial UML diagram
* Implement model classes

Week 2 - Core Features
* Implement collection mgmt
* Filter, sort and search logic
* Begin controller layer

Week 3 - GUI integration
* Build Swing UX
* Integrate Swing UX with controller
* Implement save feature

Week 4 - Feature Completion
* Implement any outstanding features
* Polish UI
* Debugging
* Documentation (e.g. - README, manual, final UML)

Week 5 - Finalization
* Documentation (e.g. - README, manual, final UML)
* Demo prep

Checkpoints
* Team will meet each weekend + adhoc.
