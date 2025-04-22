package com.movieapp;

public class Movie {
    private final int id;
    private final String title;
    private final String overview;
    private final String releaseDate;
    private final float rating;
    private final String posterPath;
    private String director;
    private String[] cast;
    private String[] producers;
    private String runtime;
    private String[] genres;

    // Constructor for basic movie info (used in search results)
    public Movie(int id, String title, String overview, String releaseDate, float rating, String posterPath) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.posterPath = posterPath;
    }

    // Method to update movie with additional details
    public void setAdditionalDetails(String director, String[] cast, String[] producers, String runtime, String[] genres) {
        this.director = director;
        this.cast = cast;
        this.producers = producers;
        this.runtime = runtime;
        this.genres = genres;
    }

    //  getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public float getRating() { return rating; }
    public String getPosterPath() { return posterPath; }
    public String getDirector() { return director; }
    public String[] getCast() { return cast; }
    public String[] getProducers() { return producers; }
    public String getRuntime() { return runtime; }
    public String[] getGenres() { return genres; }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", rating=" + rating +
                '}';
    }
} 
