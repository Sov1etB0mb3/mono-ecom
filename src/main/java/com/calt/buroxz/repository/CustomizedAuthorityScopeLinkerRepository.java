package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.AuthorityScopeLinker;
import com.calt.buroxz.domain.Scope;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AuthorityScopeLinker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomizedAuthorityScopeLinkerRepository {
    List<AuthorityScopeLinker> findAuthLinkerWithScopes(Collection<String> authorityNames);
    boolean existsLinkerByAuthAndScope(Authority authority, Scope scope);
}
