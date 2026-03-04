package com.example.securitymicroservice.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = {"role", "role.authorities", "authorities"})
    Optional<AppUser> findByUsername(String username);
}
