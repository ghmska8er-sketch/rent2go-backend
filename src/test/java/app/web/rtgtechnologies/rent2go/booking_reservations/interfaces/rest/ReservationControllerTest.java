package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices.ReservationCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices.ReservationQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.UpdateReservationStatusCommand;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.CancelReservationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.ConfirmReturnCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.CreateReservationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.ModifyReservationCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.ReservationResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.UpdateReservationStatusCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.UpdateReservationStatusResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * ReservationControllerTest
 *
 * TS07: unit tests verifying POST /reservations/{id}/status converts domain exceptions
 * (invalid transitions) into HTTP 409/400 responses instead of letting them propagate as a
 * raw 500, mirroring the existing confirm/activate/complete sibling endpoints' behavior.
 * All collaborators are mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationCommandServiceImpl commandService;
    @Mock
    private ReservationQueryServiceImpl queryService;
    @Mock
    private CreateReservationCommandFromResourceAssembler commandAssembler;
    @Mock
    private ReservationResourceFromEntityAssembler resourceAssembler;
    @Mock
    private CancelReservationCommandFromResourceAssembler cancelAssembler;
    @Mock
    private ModifyReservationCommandFromResourceAssembler modifyAssembler;
    @Mock
    private UpdateReservationStatusCommandFromResourceAssembler updateStatusAssembler;
    @Mock
    private ConfirmReturnCommandFromResourceAssembler confirmReturnAssembler;

    private ReservationController controller;

    @BeforeEach
    void setUp() {
        controller = new ReservationController(
                commandService,
                queryService,
                commandAssembler,
                resourceAssembler,
                cancelAssembler,
                modifyAssembler,
                updateStatusAssembler,
                confirmReturnAssembler
        );
    }

    @Test
    void updateReservationStatus_returnsConflict_whenTransitionIsIllegalState() {
        var resource = new UpdateReservationStatusResource();
        resource.setActorId(1L);
        resource.setStatus("ACTIVE");
        var command = new UpdateReservationStatusCommand(10L, 1L, "ACTIVE");

        when(updateStatusAssembler.toCommand(10L, resource)).thenReturn(command);
        when(commandService.handle(command)).thenThrow(new IllegalStateException("Reservation must be confirmed before activation"));

        var response = controller.updateReservationStatus(10L, resource);

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void updateReservationStatus_returnsBadRequest_whenStatusValueIsInvalid() {
        var resource = new UpdateReservationStatusResource();
        resource.setActorId(1L);
        resource.setStatus("NOT_A_REAL_STATUS");
        var command = new UpdateReservationStatusCommand(10L, 1L, "NOT_A_REAL_STATUS");

        when(updateStatusAssembler.toCommand(10L, resource)).thenReturn(command);
        when(commandService.handle(command)).thenThrow(new IllegalArgumentException("Unsupported target status: NOT_A_REAL_STATUS"));

        var response = controller.updateReservationStatus(10L, resource);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void updateReservationStatus_returnsOk_whenTransitionIsValid() {
        var resource = new UpdateReservationStatusResource();
        resource.setActorId(1L);
        resource.setStatus("CONFIRMED");
        var command = new UpdateReservationStatusCommand(10L, 1L, "CONFIRMED");
        var updatedReservation = org.mockito.Mockito.mock(app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.aggregates.Reservation.class);

        when(updateStatusAssembler.toCommand(10L, resource)).thenReturn(command);
        when(commandService.handle(command)).thenReturn(updatedReservation);
        when(resourceAssembler.toResource(updatedReservation)).thenReturn(
                new app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.ReservationResource(
                        10L, "CODE", 1L, 2L, 3L, null, null, null, "CONFIRMED", null, null, null, null, null, null, null, null));

        var response = controller.updateReservationStatus(10L, resource);

        assertEquals(200, response.getStatusCode().value());
    }
}
