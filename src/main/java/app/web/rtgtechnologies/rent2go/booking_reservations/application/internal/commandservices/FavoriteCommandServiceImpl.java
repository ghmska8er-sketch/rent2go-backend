package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Favorite;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.AddFavoriteCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.RemoveFavoriteCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.FavoriteCommandService;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.FavoriteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class FavoriteCommandServiceImpl implements FavoriteCommandService {

    private final FavoriteRepository favoriteRepository;

    @Override
    public Favorite handle(AddFavoriteCommand command) {
        favoriteRepository.findByRenterIdAndVehicleId(command.renterId(), command.vehicleId())
            .ifPresent(existing -> { throw new IllegalArgumentException("Favorite already exists"); });

        Favorite fav = Favorite.of(command.renterId(), command.vehicleId());
        return favoriteRepository.save(fav);
    }

    @Override
    public void handle(RemoveFavoriteCommand command) {
        favoriteRepository.deleteByRenterIdAndVehicleId(command.renterId(), command.vehicleId());
    }
}
