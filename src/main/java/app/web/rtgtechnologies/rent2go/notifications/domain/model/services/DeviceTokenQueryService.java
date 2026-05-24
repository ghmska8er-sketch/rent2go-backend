package app.web.rtgtechnologies.rent2go.notifications.domain.model.services;

import app.web.rtgtechnologies.rent2go.notifications.domain.model.aggregates.DeviceToken;
import app.web.rtgtechnologies.rent2go.notifications.domain.model.queries.GetDeviceTokensByUserQuery;

import java.util.List;

public interface DeviceTokenQueryService {
    List<DeviceToken> handle(GetDeviceTokensByUserQuery query);
}