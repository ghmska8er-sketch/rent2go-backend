package app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest;

import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.commandservices.FavoriteCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.application.internal.queryservices.FavoriteQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.AddFavoriteCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.assemblers.FavoriteResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.AddFavoriteResource;
import app.web.rtgtechnologies.rent2go.booking_reservations.interfaces.rest.resources.FavoriteResource;
import app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resource.PagedResponse;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/favorites")
@AllArgsConstructor
@Validated
public class FavoritesController {

    private final FavoriteCommandServiceImpl commandService;
    private final FavoriteQueryServiceImpl queryService;
    private final AddFavoriteCommandFromResourceAssembler assembler;
    private final FavoriteResourceFromEntityAssembler resourceAssembler;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FavoriteResource> addFavorite(@RequestBody @Valid AddFavoriteResource resource) {
        var command = assembler.toCommand(resource);
        var saved = commandService.handle(command);
        var resp = resourceAssembler.toResource(saved);
        return ResponseEntity.created(URI.create("/api/v1/favorites/" + saved.getId())).body(resp);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFavorite(
        @RequestParam @Positive(message = "User ID must be positive") Long userId,
        @RequestParam @Positive(message = "Vehicle ID must be positive") Long vehicleId
    ) {
        commandService.handle(new app.web.rtgtechnologies.rent2go.booking_reservations.domain.model.commands.RemoveFavoriteCommand(userId, vehicleId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<FavoriteResource>> listFavorites(
        @RequestParam @Positive(message = "User ID must be positive") Long userId,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        var results = queryService.findByUserId(userId);
        return ResponseEntity.ok(toPagedResponse(results, page, size, resourceAssembler::toResource));
    }

    private <T, R> PagedResponse<R> toPagedResponse(List<T> source, int page, int size, Function<T, R> mapper) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        long totalElements = source.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, source.size());
        int toIndex = Math.min(fromIndex + safeSize, source.size());
        List<R> content = source.subList(fromIndex, toIndex).stream().map(mapper).toList();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        return new PagedResponse<>(content, safePage, safeSize, totalElements, totalPages);
    }
}
