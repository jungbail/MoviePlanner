package view;

import model.GenreFilterHelper;
import model.Movie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.net.URI;
import java.net.HttpURLConnection;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;


public class View extends JFrame implements IView {
    /** These fields store the date components used in the Release Date dropdown. */
    private JComboBox<String> monthBox;
    private JComboBox<String> dayBox;
    private JComboBox<String> yearBox;

    /** Set some standard values for border padding. */
    private static final int GAP = 12;
    private static final int OUTER_PADDING = 16;

    /** Panels for the watchlist and working movie list. */
    private JPanel activeListPanel, browseListPanel;

    /** These are the two List Models and two JLists that will be used in the two side panels. */
    private final DefaultListModel<String> movieListModel = new DefaultListModel<>();
    private final DefaultListModel<String> browseListModel = new DefaultListModel<>();
    private JList<String> movieList, browseMovieList;
    private JScrollPane browseMovieListScroll, movieListScroll;

    /** All the buttons for the program */
    private JButton addButton, removeButton, resetButton, exportButton, includeButton, excludeButton, sortGenre, sortTitle, sortData, sortVote, sortPop, ascend, descend, popGreater, popLesser, voteGreater, voteLesser, dateAfter, dateBefore, searchButton;

    /** All the text Fields for the program. fileInput is for exporting to a file/path. */
    private JTextField fileInput, popularityInput, voteInput, searchTitle;

    /** Read-only overview shown under the search bar when a title search succeeds. */
    private JTextArea searchOverviewArea, searchPopularityArea, searchVoteAverageArea, searchReleaseDateArea;

    /** The JTabbedPane that holds the different filtering operations. Built out in {@link #buildCenterArea()} */
    private JTabbedPane filteringParams;

    /** These are the different tab panels or components of {@code filteringParams} used in {@link #buildCenterArea()}*/
    private JComponent genrePanel, searchPanel, releaseDatePanel, votePanel, popPanel;
    /** The ComboBox that keeps all the genres available for the user in {@code genrePanel} used in {@link #genreFilteringPanel()}*/
    private JComboBox<String> genreComboBox;

    /** Used for {@code genreComboBox} in {@link #genreFilteringPanel()} to get an array of all genres. */
    private final String[] genreStrings = GenreFilterHelper.allGenreNamesSorted().toArray(new String[0]);

    /** Poster thumbnail on the Search by Title tab; updated from {@link #setPosterPath(String)}. */
    private JLabel searchPosterLabel;


    /**
     * Constructor for the View
     * @param caption for the frame
     */
    public View(String caption){
        super(caption);
        configureFrame();
        buildMainDisplay();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Gives View initial sizing and exit buttons.
     */
    private void configureFrame(){
        setMinimumSize(new Dimension(1000, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(GAP, GAP));
    }

    /**
     * Main Builder of the program.
     * Links all the different elements of the View.
     */
    private void buildMainDisplay() {
        add(buildHeaderPanel(), BorderLayout.NORTH);
        buildActiveListPanel();
        buildBrowseListPanel();
        buildCenterArea();
        add(buildControlPanel(), BorderLayout.SOUTH);
    }

    /**
     * Builds the top header area containing the application name and descriptive text.
     * @return the header.
     */
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(OUTER_PADDING, OUTER_PADDING * 2, OUTER_PADDING, OUTER_PADDING * 2));
        JLabel headerLabel = new JLabel(buildWelcomeMessage(), SwingConstants.CENTER);
        header.add(headerLabel, BorderLayout.CENTER);
        return header;
    }

    /**
     * Panel for resetting the working movie list and exporting the Watchlist
     * @return control panel
     */
    private JPanel buildControlPanel() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, GAP / 2));
        controls.setBorder(BorderFactory.createEmptyBorder(0, OUTER_PADDING, OUTER_PADDING, OUTER_PADDING));

        fileInput = new JTextField(30);
        controls.add(fileInput);

        exportButton = new JButton("Export");
        exportButton.setActionCommand("Export Button");
        controls.add(exportButton);

        resetButton = new JButton("Reset Working Movie List");
        resetButton.setActionCommand("Reset Button");
        controls.add(resetButton);

        return controls;
    }

    /**
     * Builds the Watchlist panel
     */
    private void buildActiveListPanel(){
        this.movieList = new JList<>(movieListModel);
        this.movieListScroll = new JScrollPane(movieList);
        this.activeListPanel = new JPanel(new BorderLayout());
        activeListPanel.setBorder(BorderFactory.createTitledBorder("Your Watchlist"));
        activeListPanel.setMinimumSize(new Dimension(260, 300));
        activeListPanel.setFocusable(true);
        activeListPanel.add(this.movieListScroll, BorderLayout.CENTER);

        JPanel eastWrapper = new JPanel(new BorderLayout());
        eastWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, OUTER_PADDING, OUTER_PADDING));
        eastWrapper.setPreferredSize(new Dimension(320, 0));
        eastWrapper.add(this.activeListPanel, BorderLayout.CENTER);
        add(eastWrapper, BorderLayout.EAST);
    }

    /**
     * Left column: all movies (or filtered subset); same chrome as the watchlist panel.
     * Populate {@link #browseListModel} from the controller when data is loaded.
     */
    private void buildBrowseListPanel() {
        this.browseMovieList = new JList<>(browseListModel);
        this.browseMovieListScroll = new JScrollPane(browseMovieList);
        this.browseListPanel = new JPanel(new BorderLayout());
        browseListPanel.setBorder(BorderFactory.createTitledBorder("Working Movie List"));
        browseListPanel.setMinimumSize(new Dimension(260, 300));
        browseListPanel.setPreferredSize(new Dimension(320, 0));
        browseListPanel.setFocusable(true);
        browseListPanel.add(browseMovieListScroll, BorderLayout.CENTER);
    }

    /**
     * Builds the area where filtering and building the Watchlist will take place.
     * Builds a JTabbedPane with the following JComponents:
     * {@link #genreFilteringPanel()}
     * {@link #releaseDateFilteringPanel()}
     * {@link #searchFilteringPanel()}
     * {@link #popularityFilteringPanel()}
     * {@link #voteFilteringPanel()}
     *
     */
    private void buildCenterArea() {
        this.filteringParams = new JTabbedPane();
        this.genrePanel = genreFilteringPanel();
        this.filteringParams.addTab("Genre", this.genrePanel);
        this.filteringParams.setMnemonicAt(0, KeyEvent.VK_1);

        this.popPanel = popularityFilteringPanel();
        this.filteringParams.addTab("Popularity", this.popPanel);
        this.filteringParams.setMnemonicAt(1, KeyEvent.VK_2);

        this.releaseDatePanel = releaseDateFilteringPanel();
        this.filteringParams.addTab("Release Date", this.releaseDatePanel);
        this.filteringParams.setMnemonicAt(2, KeyEvent.VK_3);

        this.votePanel = voteFilteringPanel();
        this.filteringParams.addTab("Vote", this.votePanel);
        this.filteringParams.setMnemonicAt(3, KeyEvent.VK_4);

        this.searchPanel = searchFilteringPanel();
        this.filteringParams.addTab("Search by Title", this.searchPanel);
        this.filteringParams.setMnemonicAt(4, KeyEvent.VK_5);

        this.addButton = new JButton("Add Selected Movies");
        this.addButton.setActionCommand("Add selected movies");

        this.removeButton = new JButton("Remove Selected Movies");
        this.removeButton.setActionCommand("Remove Selected Movies");

        this.sortGenre = new JButton("Sort by Genre");
        this.sortGenre.setActionCommand("Sort by Genre");
        this.sortTitle = new JButton("Sort by Title");
        this.sortTitle.setActionCommand("Sort by Title");
        this.sortData = new JButton("Sort by Data");
        this.sortData.setActionCommand("Sort by Data");
        this.sortVote = new JButton("Sort by Vote");
        this.sortVote.setActionCommand("Sort by Vote");
        this.sortPop = new JButton("Sort by Popularity");
        this.sortPop.setActionCommand("Sort by Popularity");
        this.ascend = new JButton("Sort Ascending");
        this.ascend.setActionCommand("Sort Ascending");
        this.descend = new JButton("Sort Descending");
        this.descend.setActionCommand("Sort Descending");

        JPanel sortingRow = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP / 3, GAP / 3));
        sortingRow.setBorder(BorderFactory.createEmptyBorder(GAP, 0, 0, 0));
        sortingRow.add(this.sortGenre);
        sortingRow.add(this.sortTitle);
        sortingRow.add(this.sortData);
        sortingRow.add(this.sortVote);
        sortingRow.add(this.sortPop);
        sortingRow.add(this.ascend);
        sortingRow.add(this.descend);

        JPanel addButtonRow = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, GAP / 2));
        addButtonRow.setBorder(BorderFactory.createEmptyBorder(GAP, 0, 0, 0));
        addButtonRow.add(this.addButton);
        addButtonRow.add(this.removeButton);

        JPanel buttonControls = new JPanel(new BorderLayout());
        buttonControls.add(sortingRow, BorderLayout.SOUTH);
        buttonControls.add(addButtonRow, BorderLayout.NORTH);

        JPanel tabColumn = new JPanel(new BorderLayout());
        tabColumn.add(this.filteringParams, BorderLayout.CENTER);
        tabColumn.add(buttonControls, BorderLayout.PAGE_END);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, OUTER_PADDING, OUTER_PADDING, 0));
        centerWrapper.add(this.browseListPanel, BorderLayout.WEST);
        centerWrapper.add(tabColumn, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);
    }


    /** Builds the genre filtering controls. */
    private JComponent genreFilteringPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, GAP / 2));
        panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        panel.add(new JLabel("Genre:"));

        this.genreComboBox = new JComboBox<>(genreStrings);
        if (genreComboBox.getItemCount() > 0) {
            genreComboBox.setSelectedIndex(0);
        }
        genreComboBox.setActionCommand("genreChanged");
        panel.add(genreComboBox);

        this.includeButton = new JButton("Filter Including Genre");
        this.includeButton.setActionCommand("Filter Including Genre");
        panel.add(includeButton);

        this.excludeButton = new JButton("Filter Excluding Genre");
        this.excludeButton.setActionCommand("Filter Excluding Genre");
        panel.add(excludeButton);

        return panel;
    }

    /** Builds the title search input field, search button, and description area below. */
    private JComponent searchFilteringPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, GAP));
        outer.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, GAP / 2));
        row.add(new JLabel("Search:"));
        searchTitle = new JTextField(20);
        this.searchButton = new JButton("Search Title");
        searchButton.setActionCommand("Search Title");
        row.add(searchTitle);
        row.add(searchButton);
        outer.add(row, BorderLayout.NORTH);

        searchOverviewArea = new JTextArea(2, 36);
        searchOverviewArea.setEditable(false);
        searchOverviewArea.setLineWrap(true);
        searchOverviewArea.setWrapStyleWord(true);
        JScrollPane overviewScroll = new JScrollPane(searchOverviewArea);
        overviewScroll.setBorder(BorderFactory.createTitledBorder("Description"));
        outer.add(overviewScroll, BorderLayout.CENTER);

        searchPopularityArea = new JTextArea(1, 36);
        searchPopularityArea.setEditable(false);
        searchPopularityArea.setLineWrap(true);
        searchPopularityArea.setWrapStyleWord(true);
        searchPopularityArea.setBorder(BorderFactory.createTitledBorder("Popularity"));

        searchVoteAverageArea = new JTextArea(1, 36);
        searchVoteAverageArea.setEditable(false);
        searchVoteAverageArea.setLineWrap(true);
        searchVoteAverageArea.setWrapStyleWord(true);
        searchVoteAverageArea.setBorder(BorderFactory.createTitledBorder("Average vote"));

        searchReleaseDateArea = new JTextArea(1, 36);
        searchReleaseDateArea.setEditable(false);
        searchReleaseDateArea.setLineWrap(true);
        searchReleaseDateArea.setWrapStyleWord(true);
        searchReleaseDateArea.setBorder(BorderFactory.createTitledBorder("Release date"));

        JPanel searchMetrics = new JPanel(new GridLayout(3, 1, 0, GAP / 2));
        searchMetrics.add(searchPopularityArea);
        searchMetrics.add(searchVoteAverageArea);
        searchMetrics.add(searchReleaseDateArea);

        JPanel leftColumn = new JPanel(new BorderLayout(0, GAP));
        leftColumn.add(overviewScroll, BorderLayout.CENTER);
        leftColumn.add(searchMetrics, BorderLayout.SOUTH);

        searchPosterLabel = new JLabel("", SwingConstants.CENTER);
        searchPosterLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel searchBody = new JPanel(new BorderLayout(GAP, 0));
        searchBody.add(leftColumn, BorderLayout.CENTER);
        searchBody.add(searchPosterLabel, BorderLayout.EAST);

        outer.add(searchBody, BorderLayout.CENTER);

        return outer;
    }

    /** Builds the release date selectors and Before and After buttons. */
    private JComponent releaseDateFilteringPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, GAP / 2));
        panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        panel.add(new JLabel("Release Date:"));

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 0));

        this.monthBox = new JComboBox<>(new String[]{
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        });
        this.dayBox = new JComboBox<>(new String[]{
                "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"
        });
        this.yearBox = new JComboBox<>(buildYearOptions());

        dateRow.add(monthBox);
        dateRow.add(dayBox);
        dateRow.add(yearBox);
        panel.add(dateRow);

        this.dateBefore = new JButton("Before");
        dateBefore.setActionCommand("Before Date");
        panel.add(dateBefore);

        this.dateAfter = new JButton("After");
        dateAfter.setActionCommand("After Date");
        panel.add(dateAfter);

        return panel;
    }

    /** Generates the year options for the current year back to 1888. */
    private String[] buildYearOptions() {
        int currentYear = Year.now().getValue();
        int minYear = 1888;
        int size = currentYear - minYear + 1;
        String[] years = new String[size];

        for (int i = 0; i < size; i++) {
            years[i] = Integer.toString(currentYear - i);
        }
        return years;
    }

    /** Builds the vote filter input and comparison buttons. */
    private JComponent voteFilteringPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, GAP / 2));
        panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        panel.add(new JLabel("Average Vote:"));

        this.voteInput = new JTextField(10);
        panel.add(voteInput);

        this.voteLesser = new JButton("Vote Lesser Than");
        this.voteLesser.setActionCommand("Vote Lesser Than");
        panel.add(voteLesser);
        this.voteGreater = new JButton("Vote Greater Than");
        this.voteGreater.setActionCommand("Vote Greater Than");
        panel.add(voteGreater);
        return panel;
    }

    /** Builds the popularity filter input and comparison buttons. */
    private JComponent popularityFilteringPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, GAP / 2));
        panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        panel.add(new JLabel("Popularity:"));
        this.popularityInput = new JTextField(10);
        panel.add(popularityInput);

        this.popLesser = new JButton("Popularity Lesser Than");
        this.popLesser.setActionCommand("Popularity Lesser Than");
        panel.add(popLesser);
        this.popGreater = new JButton("Popularity Greater Than");
        this.popGreater.setActionCommand("Popularity Greater Than");
        panel.add(popGreater);

        return panel;
    }

    @Override
    public void setListener(ActionListener actionEvent) {
        exportButton.addActionListener(actionEvent);
        resetButton.addActionListener(actionEvent);
        genreComboBox.addActionListener(actionEvent);
        addButton.addActionListener(actionEvent);
        removeButton.addActionListener(actionEvent);
        includeButton.addActionListener(actionEvent);
        excludeButton.addActionListener(actionEvent);
        sortGenre.addActionListener(actionEvent);
        sortTitle.addActionListener(actionEvent);
        sortData.addActionListener(actionEvent);
        sortVote.addActionListener(actionEvent);
        sortPop.addActionListener(actionEvent);
        ascend.addActionListener(actionEvent);
        descend.addActionListener(actionEvent);
        popGreater.addActionListener(actionEvent);
        popLesser.addActionListener(actionEvent);
        voteGreater.addActionListener(actionEvent);
        voteLesser.addActionListener(actionEvent);
        dateAfter.addActionListener(actionEvent);
        dateBefore.addActionListener(actionEvent);
        searchTitle.addActionListener(actionEvent);
        searchButton.addActionListener(actionEvent);
    }


    /**
     * Helper method for building the program's welcome message (title + description).
     * @return The welcome message with HTML formatting applied.
     */
    private String buildWelcomeMessage() {
        return "<html><div style='text-align:center;'>"
                + "<h2>Welcome to Movie Watchlist Planner</h2>"
                + "<p>This program has a local database of movies which you can filter through in bulk or one-by-one. "
                + "If you can't find what you are looking for, you can make an API request to add it to the local database.</p>"
                + "</div></html>";
    }

    @Override
    public String getSelectedGenreName() {
        return genreComboBox.getSelectedItem().toString();
    }

    @Override
    public String getExportFile() {
        return fileInput.getText();
    }

    @Override
    public String getSearchTitle() {
        return searchTitle.getText();
    }

    @Override
    public void setSearchResultOverview(String overview) {
        if (overview == null || overview.isBlank()) {
            searchOverviewArea.setText("");
        } else {
            searchOverviewArea.setText(overview);
            searchOverviewArea.setCaretPosition(0);
        }
    }

    @Override
    public void setSearchResultPopularity(double popularity) {
        if (Double.isNaN(popularity)) {
            searchPopularityArea.setText("");
        } else {
            searchPopularityArea.setText(String.format("%.2f", popularity));
            searchPopularityArea.setCaretPosition(0);
        }
    }

    @Override
    public void setSearchResultVoteAverage(double voteAverage) {
        if (Double.isNaN(voteAverage)) {
            searchVoteAverageArea.setText("");
        } else {
            searchVoteAverageArea.setText(String.format("%.2f", voteAverage));
            searchVoteAverageArea.setCaretPosition(0);
        }
    }

    @Override
    public void setSearchResultReleaseDate(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank()) {
            searchReleaseDateArea.setText("");
        } else {
            searchReleaseDateArea.setText(releaseDate);
            searchReleaseDateArea.setCaretPosition(0);
        }
    }

    @Override
    public void setPosterPath(String partialPath) {
        if (searchPosterLabel == null) {
            return;
        }
        if (partialPath == null || partialPath.isBlank()) {
            searchPosterLabel.setIcon(null);
            searchPosterLabel.setText("No poster");
            return;
        }
        String imageUrl = "https://image.tmdb.org/t/p/w200" + partialPath;
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(imageUrl).toURL().openConnection();
//            try {
                BufferedImage image = ImageIO.read(connection.getInputStream());
                if (image == null) {
                    searchPosterLabel.setIcon(null);
                    searchPosterLabel.setText("No poster");
                } else {
                    searchPosterLabel.setText(null);
                    searchPosterLabel.setIcon(new ImageIcon(image));
                }
        } catch (IOException e) {
            searchPosterLabel.setIcon(null);
            searchPosterLabel.setText("No poster");
        }
    }

    @Override
    public String promptForTmdbBearerToken() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Not in local database. Enter your TMDB API read access token to search online:"),
                BorderLayout.NORTH);
        JPasswordField passwordField = new JPasswordField(28);
        panel.add(passwordField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "TMDB authentication",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        char[] pass = passwordField.getPassword();
        try {
            return new String(pass).trim();
        } finally {
            Arrays.fill(pass, '\0');
        }
    }

    @Override
    public double getVote() {
        return Double.parseDouble(voteInput.getText());
    }

    @Override
    public double getPopularity() {
        return Double.parseDouble(popularityInput.getText());
    }

    @Override
    public String getReleaseDate() {
        return yearBox.getSelectedItem() + "-" +
                monthBox.getSelectedItem() + "-" +
                dayBox.getSelectedItem();
    }

    @Override
    public void setWorkingMovies(List<Movie> movies) {
        browseListModel.clear();
        for (Movie movie : movies) {
            browseListModel.addElement(movie.title());
        }
    }

    @Override
    public void setWatchlistMovies(List<Movie> movies) {
        movieListModel.clear();
        for (Movie movie : movies) {
            movieListModel.addElement(movie.title());
        }
    }

    @Override
    public List<String> getSelectedWorkingMovieTitles() {
        return browseMovieList.getSelectedValuesList();
    }

    @Override
    public List<String> getSelectedWatchlistMovieTitles() {
        return movieList.getSelectedValuesList();
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}