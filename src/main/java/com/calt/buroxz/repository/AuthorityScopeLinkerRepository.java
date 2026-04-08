package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.AuthorityScopeLinker;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AuthorityScopeLinker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorityScopeLinkerRepository extends JpaRepository<AuthorityScopeLinker, Long> {
    AuthorityScopeLinker findAuthorityScopeLinkerByAuthority(Authority authority);
}
