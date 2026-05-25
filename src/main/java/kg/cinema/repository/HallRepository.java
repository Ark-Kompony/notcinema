package kg.cinema.repository;

import kg.cinema.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    List<Hall> findByCinemaId(Long cinemaId);

    List<Hall> findByCinemaIdAndIsActiveTrue(Long cinemaId);
}
