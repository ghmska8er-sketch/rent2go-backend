package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail_Value(String email);
    Optional<User> findByUsername_Value(String username);
    boolean existsByEmail_Value(String email);
    boolean existsByUsername_Value(String username);
}
