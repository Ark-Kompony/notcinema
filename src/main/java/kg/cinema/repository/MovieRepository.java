package kg.cinema.repository;

import kg.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(String status);

    @Query("SELECT m FROM Movie m WHERE m.status = 'in_theaters' ORDER BY m.releaseDate DESC")
    List<Movie> findCurrentlyShowing();

    @Query("SELECT m FROM Movie m WHERE m.status = 'coming_soon' ORDER BY m.releaseDate ASC")
    List<Movie> findComingSoon();

    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId AND m.status = :status")
    List<Movie> findByGenreAndStatus(@Param("genreId") Long genreId, @Param("status") String status);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Movie> searchByKeyword(@Param("keyword") String keyword);
}
