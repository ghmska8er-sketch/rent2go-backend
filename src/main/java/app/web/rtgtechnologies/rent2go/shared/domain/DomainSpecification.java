package app.web.rtgtechnologies.rent2go.shared.domain;

import org.springframework.data.jpa.domain.Specification;

/**
 * Base interface for JPA Specifications
 * 
 * Used to build dynamic queries in a reusable way following DDD patterns.
 * Specifications encapsulate query logic and are composed together.
 */
public interface DomainSpecification<T> extends Specification<T> {
    
}
