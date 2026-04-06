package com.calt.buroxz.service;

import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.ScopeRepository;
import com.calt.buroxz.repository.search.ScopeSearchRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.calt.buroxz.domain.Scope}.
 */
@Service
@Transactional
public class ScopeService {

    private static final Logger LOG = LoggerFactory.getLogger(ScopeService.class);

    private final ScopeRepository scopeRepository;

    private final ScopeSearchRepository scopeSearchRepository;

    public ScopeService(ScopeRepository scopeRepository, ScopeSearchRepository scopeSearchRepository) {
        this.scopeRepository = scopeRepository;
        this.scopeSearchRepository = scopeSearchRepository;
    }

    /**
     * Save a scope.
     *
     * @param scope the entity to save.
     * @return the persisted entity.
     */
    public Scope save(Scope scope) {
        LOG.debug("Request to save Scope : {}", scope);
        scope = scopeRepository.save(scope);
        scopeSearchRepository.index(scope);
        return scope;
    }

    /**
     * Update a scope.
     *
     * @param scope the entity to save.
     * @return the persisted entity.
     */
    public Scope update(Scope scope) {
        LOG.debug("Request to update Scope : {}", scope);
        scope = scopeRepository.save(scope);
        scopeSearchRepository.index(scope);
        return scope;
    }

    /**
     * Partially update a scope.
     *
     * @param scope the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Scope> partialUpdate(Scope scope) {
        LOG.debug("Request to partially update Scope : {}", scope);

        return scopeRepository
            .findById(scope.getId())
            .map(existingScope -> {
                if (scope.getName() != null) {
                    existingScope.setName(scope.getName());
                }
                if (scope.getDescription() != null) {
                    existingScope.setDescription(scope.getDescription());
                }

                return existingScope;
            })
            .map(scopeRepository::save)
            .map(savedScope -> {
                scopeSearchRepository.index(savedScope);
                return savedScope;
            });
    }

    /**
     * Get all the scopes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @PreAuthorize("hasAuthority('scope:all')")
    @Transactional(readOnly = true)
    public Page<Scope> findAll(Pageable pageable) {
        LOG.debug("Request to get all Scopes");
        return scopeRepository.findAll(pageable);
    }

    /**
     * Get one scope by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Scope> findOne(Long id) {
        LOG.debug("Request to get Scope : {}", id);
        return scopeRepository.findById(id);
    }

    /**
     * Delete the scope by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Scope : {}", id);
        scopeRepository.deleteById(id);
        scopeSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the scope corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Scope> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Scopes for query {}", query);
        return scopeSearchRepository.search(query, pageable);
    }
}
