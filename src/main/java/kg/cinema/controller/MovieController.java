package kg.cinema.controller;

import kg.cinema.entity.Movie;
import kg.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /**
     * Get all movies
     */
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    /**
     * Get movie by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Get currently showing movies
     */
    @GetMapping("/now-showing")
    public ResponseEntity<List<Movie>> getCurrentlyShowing() {
        return ResponseEntity.ok(movieService.getCurrentlyShowing());
    }

    /**
     * Get coming soon movies
     */
    @GetMapping("/coming-soon")
    public ResponseEntity<List<Movie>> getComingSoon() {
        return ResponseEntity.ok(movieService.getComingSoon());
    }

    /**
     * Search movies by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String keyword) {
        return ResponseEntity.ok(movieService.searchMovies(keyword));
    }

    /**
     * Get movies by genre
     */
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<Movie>> getMoviesByGenre(
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "in_theaters") String status) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genreId, status));
    }

    /**
     * Create movie (ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.createMovie(movie));
    }

    /**
     * Update movie (ADMIN only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.updateMovie(id, movie));
    }

    /**
     * Delete movie (ADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
