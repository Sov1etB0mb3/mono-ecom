package com.calt.buroxz.service;

import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.ScopeRepository;
import com.calt.buroxz.repository.search.ScopeSearchRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Scope}.
 */
@Service
@Transactional
@Primary
public class CustomizedScopeService extends ScopeService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedScopeService.class);

    private final ScopeRepository scopeRepository;

    private final ScopeSearchRepository scopeSearchRepository;

    public CustomizedScopeService(ScopeRepository scopeRepository, ScopeSearchRepository scopeSearchRepository) {
        super(scopeRepository, scopeSearchRepository);
        this.scopeRepository = scopeRepository;
        this.scopeSearchRepository = scopeSearchRepository;
    }

    /**
     * Get all the scopes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @PreAuthorize("@authorizationService.hasScope('scope:all')")
    @Transactional(readOnly = true)
    public Page<Scope> findAll(Pageable pageable) {
        LOG.debug("Request to get all Scopes");
        return scopeRepository.findAll(pageable);
    }
}
