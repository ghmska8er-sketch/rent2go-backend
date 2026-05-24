package app.web.rtgtechnologies.rent2go.payments.interfaces.rest;

import app.web.rtgtechnologies.rent2go.payments.application.internal.services.PromoService;
import app.web.rtgtechnologies.rent2go.payments.domain.model.entities.PromoCode;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.CreatePromoRequest;
import app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources.PromoCodeResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments/promocodes")
@Validated
public class PromoCodeController {

    private final PromoService promoService;

    public PromoCodeController(PromoService promoService) {
        this.promoService = promoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoCodeResource> create(@Valid @RequestBody CreatePromoRequest req) {
        LocalDateTime expires = null;
        if (req.getExpiresAt() != null) {
            try { expires = LocalDateTime.parse(req.getExpiresAt()); } catch (DateTimeParseException ex) { return ResponseEntity.badRequest().build(); }
        }
        PromoCode p = promoService.createPromo(req.getCode(), req.getPercentage(), expires);
        PromoCodeResource r = new PromoCodeResource();
        r.setCode(p.getCode()); r.setPercentage(p.getPercentage()); r.setActive(p.isActive());
        r.setExpiresAt(p.getExpiresAt() == null ? null : p.getExpiresAt().toString());
        return ResponseEntity.status(201).body(r);
    }

    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable @NotBlank(message = "Code is required") String code) {
        boolean ok = promoService.deactivateByCode(code);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{code}")
    public ResponseEntity<PromoCodeResource> get(@PathVariable @NotBlank(message = "Code is required") String code) {
        Optional<app.web.rtgtechnologies.rent2go.payments.domain.model.valueobjects.Discount> opt = promoService.findActiveDiscountByCode(code);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        PromoCodeResource r = new PromoCodeResource();
        r.setCode(d.getCode()); r.setPercentage(d.getPercentage()); r.setActive(true);
        return ResponseEntity.ok(r);
    }
}
