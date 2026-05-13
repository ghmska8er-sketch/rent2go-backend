package app.web.rtgtechnologies.rent2go.shared.domain;

import java.io.Serializable;

/**
 * Base class for Value Objects
 * 
 * Value Objects are immutable, have no identity, and equality is based on their attributes.
 * Extend this class to create domain-specific value objects.
 */
public abstract class ValueObject implements Serializable {

    /**
     * Compare two value objects by their attribute values
     */
    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}
