package app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findAllByUserId(Long userId);

    Optional<Favorite> findByUserIdAndVehicleId(Long userId, Long vehicleId);

    void deleteByUserIdAndVehicleId(Long userId, Long vehicleId);
}
