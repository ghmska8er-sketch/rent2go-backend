package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices.FavoriteCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices.FavoriteQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.AddFavoriteCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.FavoriteResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.AddFavoriteResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.FavoriteResource;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/favorites")
@AllArgsConstructor
public class FavoritesController {

    private final FavoriteCommandServiceImpl commandService;
    private final FavoriteQueryServiceImpl queryService;
    private final AddFavoriteCommandFromResourceAssembler assembler;
    private final FavoriteResourceFromEntityAssembler resourceAssembler;

    @PostMapping
    public ResponseEntity<FavoriteResource> addFavorite(@RequestBody AddFavoriteResource resource) {
        var command = assembler.toCommand(resource);
        var saved = commandService.handle(command);
        var resp = resourceAssembler.toResource(saved);
        return ResponseEntity.created(URI.create("/api/v1/favorites/" + saved.getId())).body(resp);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(@RequestParam Long renterId, @RequestParam Long vehicleId) {
        commandService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.RemoveFavoriteCommand(renterId, vehicleId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<java.util.List<FavoriteResource>> listFavorites(@RequestParam Long renterId) {
        var results = queryService.findByRenterId(renterId);
        var payload = results.stream().map(resourceAssembler::toResource).toList();
        return ResponseEntity.ok(payload);
    }
}
