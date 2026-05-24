package app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Favorite;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.services.FavoriteQueryService;
import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.FavoriteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FavoriteQueryServiceImpl implements FavoriteQueryService {

    private final FavoriteRepository favoriteRepository;

    @Override
    public List<Favorite> findByUserId(Long userId) {
        return favoriteRepository.findAllByUserId(userId);
    }
}
