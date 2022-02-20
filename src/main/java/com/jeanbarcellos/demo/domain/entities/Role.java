package com.jeanbarcellos.demo.domain.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.jeanbarcellos.demo.core.domain.AggregateRoot;
import com.jeanbarcellos.demo.core.domain.Entity;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.Setter;

@javax.persistence.Entity
@Setter
@Getter
@Table(name = "role", uniqueConstraints = { @UniqueConstraint(name = "role_name_uk", columnNames = { "name" }) })
public class Role extends Entity implements AggregateRoot, GrantedAuthority {

    public static String NAME_PREFIX = "ROLE_";

    // #region Properties

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER) // fix: EAGER por causa do Hibernate
    @JoinTable(name = "role_hierarchy", joinColumns = @JoinColumn(name = "parent_role_id", columnDefinition = "uuid"), foreignKey = @ForeignKey(name = "role_hierarchy_parent_role_id_fk"), inverseJoinColumns = @JoinColumn(name = "child_role_id", columnDefinition = "uuid"), inverseForeignKey = @ForeignKey(name = "role_hierarchy_child_role_id_fk"))
    private Set<Role> childRoles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_hierarchy", joinColumns = @JoinColumn(name = "child_role_id"), inverseJoinColumns = @JoinColumn(name = "parent_role_id"))
    private Set<Role> parentRoles = new HashSet<>();

    // #endregion

    // #region Contructors

    protected Role() {
    }

    public Role(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    public Role(UUID id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    // #endregion

    // #region Equals and ToString

    @Override
    public String toString() {
        return "Role [id=" + id + ", name=" + name + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Role other = (Role) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    // #endregion

    // #region Implements GrantedAuthority

    @Override
    public String getAuthority() {
        return this.name;
    }

    // #endregion

    // #region Handler Child and Parent roles

    public Set<Role> getChildRoles() {
        return Collections.unmodifiableSet(this.childRoles);
    }

    public Role addChild(Role role) {
        if (!this.getName().equals(role.getName())) {
            this.childRoles.add(role);
            // role.parentRoles.add(this); // Erro na persistência
        }

        return this;
    }

    public boolean hasChild(Role role) {
        return this.childRoles.contains(role);
    }

    public boolean hasChild(String roleName) {
        return this.childRoles.contains(new Role(roleName, ""));
    }

    public Role clearChildRoles() {
        this.childRoles.clear();
        return this;
    }

    public Set<Role> getParentRoles() {
        return Collections.unmodifiableSet(this.parentRoles);
    }

    public Role addParent(Role role) {
        if (!this.getName().equals(role.getName())) {
            this.parentRoles.add(role);
            // role.childRoles.add(this); // Erro na persistência
        }

        return this;
    }

    public boolean hasParent(Role role) {
        return this.parentRoles.contains(role);
    }

    public boolean hasParent(String roleName) {
        return this.parentRoles.contains(new Role(roleName, ""));
    }

    public Role clearParentRoles() {
        this.parentRoles.clear();
        return this;
    }

    public Set<Role> getReachableRoles() {
        var reachableRoles = new HashSet<Role>();

        for (Role childRole : this.childRoles) {
            reachableRoles.add(childRole);

            if (!childRole.getChildRoles().isEmpty()) {
                reachableRoles.addAll(childRole.getReachableRoles());
            }
        }

        return reachableRoles;
    }

    // #endregion
}