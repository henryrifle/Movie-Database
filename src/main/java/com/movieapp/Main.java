package com.movieapp;


//API Key: 63a0c8bbfb26e63c0f9283b1450a1958

//API Read Access Token: eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI2M2EwYzhiYmZiM
//jZlNjNjMGY5MjgzYjE0NTBhMTk1OCIsIm5iZiI6MTc0MzYxMjY0Mi4zNjksInN1YiI6IjY3ZWQ2YWU
//yODM2YzhlZGE3Y2FhZmZkNSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.4moTKPNXh0n
//8RU0uNFXSP47Oo9wybKbI0XBPcQS-hpI

 

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    private static final String API_KEY = "63a0c8bbfb26e63c0f9283b1450a1958";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final OkHttpClient client;
    private final Gson gson;

    /**
     * Constructor for objects of class Main
     */
    public Main()
    {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public List<Movie> searchMovies(String query) throws IOException {
        String url = String.format("%s/search/movie?api_key=%s&query=%s", BASE_URL, API_KEY, query);
        
        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String jsonData = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
            JsonArray results = jsonObject.getAsJsonArray("results");
            
            List<Movie> movies = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JsonObject movieJson = results.get(i).getAsJsonObject();
                
                // Safely get values, providing defaults for null values
                int id = movieJson.has("id") ? movieJson.get("id").getAsInt() : 0;
                String title = movieJson.has("title") && !movieJson.get("title").isJsonNull() ? 
                    movieJson.get("title").getAsString() : "No title";
                String overview = movieJson.has("overview") && !movieJson.get("overview").isJsonNull() ? 
                    movieJson.get("overview").getAsString() : "No overview available";
                String releaseDate = movieJson.has("release_date") && !movieJson.get("release_date").isJsonNull() ? 
                    movieJson.get("release_date").getAsString() : "Release date unknown";
                float rating = movieJson.has("vote_average") && !movieJson.get("vote_average").isJsonNull() ? 
                    movieJson.get("vote_average").getAsFloat() : 0.0f;
                String posterPath = movieJson.has("poster_path") && !movieJson.get("poster_path").isJsonNull() ? 
                    movieJson.get("poster_path").getAsString() : null;

                Movie movie = new Movie(id, title, overview, releaseDate, rating, posterPath);
                movies.add(movie);
            }
            return movies;
        }
    }

    public Movie getMovieDetails(int movieId) throws IOException {
        // First get basic movie details
        String movieUrl = String.format("%s/movie/%d?api_key=%s", BASE_URL, movieId, API_KEY);
        // Get credits (cast and crew)
        String creditsUrl = String.format("%s/movie/%d/credits?api_key=%s", BASE_URL, movieId, API_KEY);
        
        try {
            // Get movie details
            Request movieRequest = new Request.Builder().url(movieUrl).build();
            String movieJson = client.newCall(movieRequest).execute().body().string();
            JsonObject movieData = gson.fromJson(movieJson, JsonObject.class);
            
            // Create basic movie object
            Movie movie = new Movie(
                movieData.get("id").getAsInt(),
                movieData.get("title").getAsString(),
                movieData.get("overview").getAsString(),
                movieData.get("release_date").getAsString(),
                movieData.get("vote_average").getAsFloat(),
                movieData.has("poster_path") ? movieData.get("poster_path").getAsString() : null
            );
            
            // Get credits
            Request creditsRequest = new Request.Builder().url(creditsUrl).build();
            String creditsJson = client.newCall(creditsRequest).execute().body().string();
            JsonObject creditsData = gson.fromJson(creditsJson, JsonObject.class);
            
            // Extract director from crew
            String director = "";
            JsonArray crew = creditsData.getAsJsonArray("crew");
            for (int i = 0; i < crew.size(); i++) {
                JsonObject person = crew.get(i).getAsJsonObject();
                if (person.get("job").getAsString().equals("Director")) {
                    director = person.get("name").getAsString();
                    break;
                }
            }
            
            // Extract cast (top 5)
            JsonArray castArray = creditsData.getAsJsonArray("cast");
            List<String> castList = new ArrayList<>();
            for (int i = 0; i < Math.min(5, castArray.size()); i++) {
                castList.add(castArray.get(i).getAsJsonObject().get("name").getAsString());
            }
            
            // Extract genres
            JsonArray genresArray = movieData.getAsJsonArray("genres");
            List<String> genresList = new ArrayList<>();
            for (int i = 0; i < genresArray.size(); i++) {
                genresList.add(genresArray.get(i).getAsJsonObject().get("name").getAsString());
            }

            // Get runtime
            String runtime = movieData.has("runtime") ? 
                movieData.get("runtime").getAsString() : "Unknown";
            
            // Update movie with additional details
            movie.setAdditionalDetails(
                director,
                castList.toArray(new String[0]),
                new String[]{}, // Add producers if available in API
                runtime,
                genresList.toArray(new String[0])
            );
            
            return movie;
        } catch (Exception e) {
            throw new IOException("Error fetching movie details: " + e.getMessage());
        }
    }

    public List<Movie> getTopRatedMovies() throws IOException {
        String url = String.format("%s/movie/top_rated?api_key=%s", BASE_URL, API_KEY);
        
        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String jsonData = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
            JsonArray results = jsonObject.getAsJsonArray("results");
            
            List<Movie> movies = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JsonObject movieJson = results.get(i).getAsJsonObject();
                
                // Safely get values, providing defaults for null values
                int id = movieJson.has("id") ? movieJson.get("id").getAsInt() : 0;
                String title = movieJson.has("title") && !movieJson.get("title").isJsonNull() ? 
                    movieJson.get("title").getAsString() : "No title";
                String overview = movieJson.has("overview") && !movieJson.get("overview").isJsonNull() ? 
                    movieJson.get("overview").getAsString() : "No overview available";
                String releaseDate = movieJson.has("release_date") && !movieJson.get("release_date").isJsonNull() ? 
                    movieJson.get("release_date").getAsString() : "Release date unknown";
                float rating = movieJson.has("vote_average") && !movieJson.get("vote_average").isJsonNull() ? 
                    movieJson.get("vote_average").getAsFloat() : 0.0f;
                String posterPath = movieJson.has("poster_path") && !movieJson.get("poster_path").isJsonNull() ? 
                    movieJson.get("poster_path").getAsString() : null;

                Movie movie = new Movie(id, title, overview, releaseDate, rating, posterPath);
                movies.add(movie);
            }
            return movies;
        }
    }

    public List<Movie> getRecentMovies() throws IOException {
        String url = String.format("%s/movie/now_playing?api_key=%s", BASE_URL, API_KEY);
        
        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String jsonData = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
            JsonArray results = jsonObject.getAsJsonArray("results");
            
            List<Movie> movies = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JsonObject movieJson = results.get(i).getAsJsonObject();
                
                // Safely get values, providing defaults for null values
                int id = movieJson.has("id") ? movieJson.get("id").getAsInt() : 0;
                String title = movieJson.has("title") && !movieJson.get("title").isJsonNull() ? 
                    movieJson.get("title").getAsString() : "No title";
                String overview = movieJson.has("overview") && !movieJson.get("overview").isJsonNull() ? 
                    movieJson.get("overview").getAsString() : "No overview available";
                String releaseDate = movieJson.has("release_date") && !movieJson.get("release_date").isJsonNull() ? 
                    movieJson.get("release_date").getAsString() : "Release date unknown";
                float rating = movieJson.has("vote_average") && !movieJson.get("vote_average").isJsonNull() ? 
                    movieJson.get("vote_average").getAsFloat() : 0.0f;
                String posterPath = movieJson.has("poster_path") && !movieJson.get("poster_path").isJsonNull() ? 
                    movieJson.get("poster_path").getAsString() : null;

                Movie movie = new Movie(id, title, overview, releaseDate, rating, posterPath);
                movies.add(movie);
            }
            return movies;
        }
    }
}
