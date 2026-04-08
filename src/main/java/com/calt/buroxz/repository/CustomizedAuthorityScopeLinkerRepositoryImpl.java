package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.AuthorityScopeLinker;
import com.calt.buroxz.domain.Scope;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CustomizedAuthorityScopeLinkerRepositoryImpl implements CustomizedAuthorityScopeLinkerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AuthorityScopeLinker> findAuthLinkerWithScopes(Collection<String> authorityNames) {
        return entityManager
            .createQuery(
                "SELECT l FROM AuthorityScopeLinker l" + " LEFT JOIN FETCH l.scope s" + " WHERE l.authority.name IN :authorityNames",
                AuthorityScopeLinker.class
            )
            .setParameter("authorityNames", authorityNames)
            .getResultList();
    }

    @Override
    public boolean existsLinkerByAuthAndScope(Authority authority, Scope scope) {
        Long count = entityManager
            .createQuery(
                "SELECT COUNT(linker) FROM AuthorityScopeLinker linker WHERE linker.authority = :auth AND linker.scope = :scope",
                Long.class
            )
            .setParameter("auth", authority)
            .setParameter("scope", scope)
            .getSingleResult();

        return count > 0;
    }
}
