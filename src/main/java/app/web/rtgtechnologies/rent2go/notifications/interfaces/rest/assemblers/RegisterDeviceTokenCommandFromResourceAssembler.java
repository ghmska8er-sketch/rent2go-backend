package app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.assemblers;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.commands.RegisterDeviceTokenCommand;
import app.web.rtgtechnologies.rent2go.notifications.interfaces.rest.resources.RegisterDeviceTokenResource;

public class RegisterDeviceTokenCommandFromResourceAssembler {

    public static RegisterDeviceTokenCommand toCommand(RegisterDeviceTokenResource resource) {
        return new RegisterDeviceTokenCommand(resource.userId(), resource.token(), resource.platform(), resource.deviceName());
    }
}