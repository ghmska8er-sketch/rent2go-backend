package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.Role;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.RoleType;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleType(RoleType roleType);
}
