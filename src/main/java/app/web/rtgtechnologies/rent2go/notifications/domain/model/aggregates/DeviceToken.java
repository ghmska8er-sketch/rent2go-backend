package app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.RegisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.valueobjects.DevicePlatform;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "device_tokens", uniqueConstraints = {
    @UniqueConstraint(name = "uk_device_tokens_user_token", columnNames = {"user_id", "token"})
})
@Getter
public class DeviceToken extends AuditableAbstractAggregateRoot<DeviceToken> {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "device_name", length = 120)
    private String deviceName;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    protected DeviceToken() {
    }

    public DeviceToken(RegisterDeviceTokenCommand command) {
        this.userId = command.userId();
        this.token = command.token();
        this.platform = command.platform();
        this.deviceName = command.deviceName();
        this.enabled = true;
    }

    public void refresh(RegisterDeviceTokenCommand command) {
        this.token = command.token();
        this.platform = command.platform();
        this.deviceName = command.deviceName();
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}