import controller.MovieController;
import model.MovieCollection;
import model.Watchlist;
import view.View;

public class MovieWatchListPlanner {

    private MovieWatchListPlanner() {
    }

    public static void main(String[] args) {
        MovieCollection model = new MovieCollection();
        Watchlist watchlist = new Watchlist();
        View view = new View("Movie Watchlist Planner"); // concrete impl so I can setVisible last
        new MovieController(model, watchlist, view);
        view.setVisible(true);
    }
}