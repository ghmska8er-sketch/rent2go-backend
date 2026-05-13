package app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.RoleType;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends AuditableAbstractAggregateRoot<Role> {

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true, length = 30)
    private RoleType roleType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Role() {
    }

    public Role(RoleType roleType, String description) {
        this.roleType = roleType;
        this.description = description;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
