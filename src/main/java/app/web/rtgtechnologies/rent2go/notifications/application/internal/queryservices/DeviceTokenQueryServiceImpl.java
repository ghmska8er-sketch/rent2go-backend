package app.web.rtgtechnologies.rent2go.notifications.application.internal.queryservices;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.DeviceToken;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetDeviceTokensByUserQuery;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.services.DeviceTokenQueryService;
import app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeviceTokenQueryServiceImpl implements DeviceTokenQueryService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenQueryServiceImpl(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public List<DeviceToken> handle(GetDeviceTokensByUserQuery query) {
        return deviceTokenRepository.findAllByUserIdOrderByCreatedAtDesc(query.userId());
    }
}