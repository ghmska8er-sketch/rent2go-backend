package app.web.rtgtechnologies.rent2go.notifications.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.DeviceToken;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.RegisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.UnregisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.services.DeviceTokenCommandService;
import app.web.rtgtechnologies.rent2go.notifications.infrastructure.persistence.jpa.repositories.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DeviceTokenCommandServiceImpl implements DeviceTokenCommandService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenCommandServiceImpl(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public Optional<DeviceToken> handle(RegisterDeviceTokenCommand command) {
        var existing = deviceTokenRepository.findByUserIdAndToken(command.userId(), command.token());
        if (existing.isPresent()) {
            var deviceToken = existing.get();
            deviceToken.refresh(command);
            return Optional.of(deviceTokenRepository.save(deviceToken));
        }

        return Optional.of(deviceTokenRepository.save(new DeviceToken(command)));
    }

    @Override
    public boolean handle(UnregisterDeviceTokenCommand command) {
        var deviceToken = deviceTokenRepository.findByIdAndUserId(command.deviceTokenId(), command.userId());
        if (deviceToken.isEmpty()) {
            return false;
        }

        deviceTokenRepository.delete(deviceToken.get());
        return true;
    }
}