package com.movieapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class MovieSearchGUI extends JFrame {
    private final Main tmdb;
    private JTextField searchField;
    private JPanel resultsPanel;  
    private JPanel detailsPanel;
    private List<Movie> currentResults;
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final int POSTER_WIDTH = 150;  
    private JComboBox<String> filterComboBox;  

    public MovieSearchGUI() {
        tmdb = new Main();
        setupUI();
    }

    private void setupUI() {
        setTitle("Movie Search Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search panel with filters
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        
        // Create filter combo box
        String[] filterOptions = {"Search by Name", "Top Rated", "Most Recent"};
        filterComboBox = new JComboBox<>(filterOptions);
        
        // Create search components panel
        JPanel searchComponents = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        
        // Add components to search panel
        searchComponents.add(filterComboBox, BorderLayout.WEST);
        searchComponents.add(searchField, BorderLayout.CENTER);
        searchComponents.add(searchButton, BorderLayout.EAST);
        
        mainPanel.add(searchComponents, BorderLayout.NORTH);

        // Results panel 
        resultsPanel = new JPanel(new GridLayout(0, 4, 10, 10));  // 4 posters per row
        JScrollPane resultsScrollPane = new JScrollPane(resultsPanel);
        resultsScrollPane.getVerticalScrollBar().setUnitIncrement(16);  
        mainPanel.add(resultsScrollPane, BorderLayout.CENTER);

        // Details panel
        detailsPanel = new JPanel(new BorderLayout(20, 50));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Movie Details"));
        mainPanel.add(detailsPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void performSearch() {
        try {
            String selectedFilter = (String) filterComboBox.getSelectedItem();
            List<Movie> results;

            switch (selectedFilter) {
                case "Top Rated":
                    results = tmdb.getTopRatedMovies();
                    break;
                case "Most Recent":
                    results = tmdb.getRecentMovies();
                    break;
                default:
                    String query = searchField.getText().trim();
                    if (query.isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "Please enter a movie name to search", 
                            "Search Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    results = tmdb.searchMovies(query);
                    break;
            }

            currentResults = results;
            displayResults();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching for movies: " + e.getMessage(),
                "Search Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults() {
        resultsPanel.removeAll();
        
        if (currentResults.isEmpty()) {
            resultsPanel.add(new JLabel("No movies found matching your search."));
            resultsPanel.revalidate();
            resultsPanel.repaint();
            return;
        }

        for (Movie movie : currentResults) {
            JPanel movieCard = createMovieCard(movie);
            resultsPanel.add(movieCard);
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createMovieCard(Movie movie) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setPreferredSize(new Dimension(POSTER_WIDTH, POSTER_WIDTH * 3/2));

        // Movie poster
        try {
            if (movie.getPosterPath() != null) {
                URL imageUrl = new URL(IMAGE_BASE_URL + movie.getPosterPath());
                Image image = ImageIO.read(imageUrl);
                Image scaledImage = image.getScaledInstance(POSTER_WIDTH, -1, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                card.add(imageLabel, BorderLayout.CENTER);
            } else {
                JLabel noImage = new JLabel("No poster");
                noImage.setHorizontalAlignment(JLabel.CENTER);
                card.add(noImage, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading poster");
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            card.add(errorLabel, BorderLayout.CENTER);
        }

        // Movie title
        JLabel titleLabel = new JLabel("<html><center>" + movie.getTitle() + "</center></html>");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        // Make card clickable
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                displayMovieDetails(movie);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return card;
    }

    private void displayMovieDetails(Movie movie) {
        try {
            Movie detailedMovie = tmdb.getMovieDetails(movie.getId());
            
            // Create a panel for movie details with image
            JPanel moviePanel = new JPanel(new BorderLayout(10, 10));
            
            // Create a top panel for the X button
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeButton = new JButton("X");
            closeButton.setForeground(Color.RED);
            closeButton.setFont(new Font("Arial", Font.BOLD, 16));
            closeButton.addActionListener(e -> {
                detailsPanel.removeAll();
                detailsPanel.revalidate();
                detailsPanel.repaint();
            });
            topPanel.add(closeButton);
            moviePanel.add(topPanel, BorderLayout.NORTH);
            
            // Create content panel for image and details
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            
            // Text details with enhanced information
            JTextArea detailsArea = new JTextArea();
            detailsArea.setEditable(false);
            detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            
            StringBuilder details = new StringBuilder();
            details.append(String.format("Title: %s\n\n", detailedMovie.getTitle()));
            details.append(String.format("Release Date: %s\n", detailedMovie.getReleaseDate()));
            details.append(String.format("Runtime: %s minutes\n", detailedMovie.getRuntime()));
            details.append(String.format("Rating: %.1f/10\n\n", detailedMovie.getRating()));
            
            // Add genres
            if (detailedMovie.getGenres() != null && detailedMovie.getGenres().length > 0) {
                details.append("Genres: ").append(String.join(", ", detailedMovie.getGenres())).append("\n\n");
            }
            
            // Add director
            if (detailedMovie.getDirector() != null) {
                details.append("Director: ").append(detailedMovie.getDirector()).append("\n\n");
            }
            
            // Add cast (top 5 actors)
            if (detailedMovie.getCast() != null && detailedMovie.getCast().length > 0) {
                details.append("Cast:\n");
                String[] cast = detailedMovie.getCast();
                for (int i = 0; i < Math.min(5, cast.length); i++) {
                    details.append("• ").append(cast[i]).append("\n");
                }
                details.append("\n");
            }
            
            // Add producers
            if (detailedMovie.getProducers() != null && detailedMovie.getProducers().length > 0) {
                details.append("Producers:\n");
                for (String producer : detailedMovie.getProducers()) {
                    details.append("• ").append(producer).append("\n");
                }
                details.append("\n");
            }
            
            details.append("Overview:\n").append(detailedMovie.getOverview());
            
            detailsArea.setText(details.toString());

            // Image panel
            JPanel imagePanel = new JPanel(new BorderLayout());
            try {
                if (detailedMovie.getPosterPath() != null) {
                    URL imageUrl = new URL(IMAGE_BASE_URL + detailedMovie.getPosterPath());
                    Image image = ImageIO.read(imageUrl);
                    Image scaledImage = image.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                    imagePanel.add(imageLabel, BorderLayout.CENTER);
                } else {
                    imagePanel.add(new JLabel("No poster available"), BorderLayout.CENTER);
                }
            } catch (Exception e) {
                imagePanel.add(new JLabel("Error loading poster"), BorderLayout.CENTER);
            }
            
            contentPanel.add(imagePanel, BorderLayout.WEST);
            contentPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
            
            moviePanel.add(contentPanel, BorderLayout.CENTER);

            detailsPanel.removeAll();
            detailsPanel.add(moviePanel, BorderLayout.CENTER);
            detailsPanel.revalidate();
            detailsPanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error fetching movie details: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MovieSearchGUI().setVisible(true);
        });
    }
} 