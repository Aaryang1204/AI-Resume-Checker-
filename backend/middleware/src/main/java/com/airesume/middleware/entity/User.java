package com.airesume.middleware.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.ColumnTransformer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.airesume.middleware.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Entity tells JPA this class maps to a database table — every instance = one row
// @Table explicitly sets both the table name and the schema:
//   name = "users"  → the actual table name
//   schema = "ra"   → the PostgreSQL schema (like a namespace for tables)
//   Without schema, JPA defaults to "public" and won't find our table
@Entity
@Table(name = "users", schema = "ra")
// Lombok: generates getters, setters, constructors, and builder pattern at
// compile time
// @Builder lets you do:
// User.builder().email("a@b.com").role(Role.CANDIDATE).build()
// @NoArgsConstructor is required by JPA internally to create instances via
// reflection
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// UserDetails is a Spring Security interface — implementing it here means
// Spring Security
// can use our User object directly for authentication without any extra wrapper
// class
public class User implements UserDetails {

    // UUID instead of Long auto-increment:
    // - doesn't expose how many users you have (id=1,2,3 is a security leak)
    // - can be generated without a DB roundtrip
    // @UuidGenerator is the Hibernate 6 modern way to auto-generate UUIDs on INSERT
    // updatable = false — primary keys should never change after creation
    @Id
    @GeneratedValue(generator = "UUID")
    @org.hibernate.annotations.UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Used as the login username — must be unique across all users
    @Column(nullable = false, unique = true)
    private String email;

    // Stored as a BCrypt hash (~60 chars), never plain text
    // Nullable because OAuth2 users (Google login) have no password —
    // they authenticate through Google instead
    @Column
    private String password;

    @Column(nullable = false)
    private String fullName;

    // Role stays as Java enum with @Enumerated(EnumType.STRING)
    // Works perfectly with plain VARCHAR column in PostgreSQL
    // Stores "CANDIDATE" / "RECRUITER" / "ADMIN" as readable text
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // removed columnDefinition — no pg enum dependency
    private Role role;

    // Provider stays as plain String
    // "local", "google", "github" — simple, flexible, no enum needed
    @Column(nullable = false)
    @Builder.Default
    private String provider = "local";

    // updatable = false — written once on INSERT, never touched again
    // Populated automatically by @PrePersist below, so you never set it manually
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Updated automatically on every save via @PreUpdate below
    @Column
    private LocalDateTime updatedAt;

    // @PrePersist is a JPA lifecycle hook — runs just before a new entity is
    // inserted into the DB (i.e. before the very first .save() call completes)
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // @PreUpdate runs just before an existing entity is updated in the DB
    // (i.e. before any subsequent .save() on an already-persisted user)
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- UserDetails interface — Spring Security calls these during authentication
    // ---

    // Returns the roles this user has — Spring expects the "ROLE_" prefix,
    // so CANDIDATE becomes "ROLE_CANDIDATE", which is what .hasRole("CANDIDATE")
    // checks against
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Spring Security uses this as the unique identifier for the user — we use
    // email
    @Override
    public String getUsername() {
        return email;
    }

    // These four return true for now — add real logic later if you need
    // account lockout, password expiry, or account suspension features
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}