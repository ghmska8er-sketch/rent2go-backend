package app.web.rtgtechnologies.rent2go.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration
 * 
 * Enables:
 * - Spring Data JPA repositories across all bounded contexts
 * - JPA Auditing for @CreatedDate and @LastModifiedDate annotations
 */
@Configuration
@EnableJpaRepositories(basePackages = {
    "app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories",
    "app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories",
    "app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories",
    "app.web.rtgtechnologies.rent2go.payments.infrastructure.persistence.jpa.repositories",
    "app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories"
})
@EnableJpaAuditing
public class JpaConfig {

}
