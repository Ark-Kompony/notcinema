package kg.cinema.repository;

import kg.cinema.entity.Promocode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromocodeRepository extends JpaRepository<Promocode, Long> {

    Optional<Promocode> findByCode(String code);

    Optional<Promocode> findByCodeAndIsActiveTrue(String code);
}
