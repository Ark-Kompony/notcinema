package kg.cinema.service;

import kg.cinema.entity.Movie;
import kg.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    public List<Movie> getCurrentlyShowing() {
        return movieRepository.findCurrentlyShowing();
    }

    public List<Movie> getComingSoon() {
        return movieRepository.findComingSoon();
    }

    public List<Movie> searchMovies(String keyword) {
        return movieRepository.searchByKeyword(keyword);
    }

    public List<Movie> getMoviesByGenre(Long genreId, String status) {
        return movieRepository.findByGenreAndStatus(genreId, status);
    }

    @Transactional
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateMovie(Long id, Movie movieDetails) {
        Movie movie = getMovieById(id);

        movie.setTitle(movieDetails.getTitle());
        movie.setDescription(movieDetails.getDescription());
        movie.setAgeRating(movieDetails.getAgeRating());
        movie.setDurationMin(movieDetails.getDurationMin());
        movie.setPosterUrl(movieDetails.getPosterUrl());
        movie.setTrailerUrl(movieDetails.getTrailerUrl());
        movie.setStatus(movieDetails.getStatus());
        movie.setReleaseDate(movieDetails.getReleaseDate());
        movie.setCountry(movieDetails.getCountry());
        movie.setDirector(movieDetails.getDirector());
        movie.setImdbRating(movieDetails.getImdbRating());

        if (movieDetails.getGenres() != null) {
            movie.setGenres(movieDetails.getGenres());
        }

        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }
}
