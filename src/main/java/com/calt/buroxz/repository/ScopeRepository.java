package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Scope;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Scope entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ScopeRepository extends JpaRepository<Scope, Long> {
    Scope findByName(String name);
}
