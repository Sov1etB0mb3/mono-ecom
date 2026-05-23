package com.calt.buroxz.web.rest;

import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.ScopeRepository;
import com.calt.buroxz.service.ScopeService;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import com.calt.buroxz.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.calt.buroxz.domain.Scope}.
 */
@RestController
@RequestMapping("/api/scopes")
public class ScopeResource {

    private static final Logger LOG = LoggerFactory.getLogger(ScopeResource.class);

    private static final String ENTITY_NAME = "scope";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ScopeService scopeService;

    private final ScopeRepository scopeRepository;

    public ScopeResource(ScopeService scopeService, ScopeRepository scopeRepository) {
        this.scopeService = scopeService;
        this.scopeRepository = scopeRepository;
    }

    /**
     * {@code POST  /scopes} : Create a new scope.
     *
     * @param scope the scope to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new scope, or with status {@code 400 (Bad Request)} if the scope has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Scope> createScope(@Valid @RequestBody Scope scope) throws URISyntaxException {
        LOG.debug("REST request to save Scope : {}", scope);
        if (scope.getId() != null) {
            throw new BadRequestAlertException("A new scope cannot already have an ID", ENTITY_NAME, "idexists");
        }
        scope = scopeService.save(scope);
        return ResponseEntity.created(new URI("/api/scopes/" + scope.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, scope.getId().toString()))
            .body(scope);
    }

    /**
     * {@code PUT  /scopes/:id} : Updates an existing scope.
     *
     * @param id the id of the scope to save.
     * @param scope the scope to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated scope,
     * or with status {@code 400 (Bad Request)} if the scope is not valid,
     * or with status {@code 500 (Internal Server Error)} if the scope couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Scope> updateScope(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Scope scope)
        throws URISyntaxException {
        LOG.debug("REST request to update Scope : {}, {}", id, scope);
        if (scope.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, scope.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!scopeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        scope = scopeService.update(scope);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, scope.getId().toString()))
            .body(scope);
    }

    /**
     * {@code PATCH  /scopes/:id} : Partial updates given fields of an existing scope, field will ignore if it is null
     *
     * @param id the id of the scope to save.
     * @param scope the scope to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated scope,
     * or with status {@code 400 (Bad Request)} if the scope is not valid,
     * or with status {@code 404 (Not Found)} if the scope is not found,
     * or with status {@code 500 (Internal Server Error)} if the scope couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Scope> partialUpdateScope(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Scope scope
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Scope partially : {}, {}", id, scope);
        if (scope.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, scope.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!scopeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Scope> result = scopeService.partialUpdate(scope);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, scope.getId().toString())
        );
    }

    /**
     * {@code GET  /scopes} : get all the scopes.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of scopes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Scope>> getAllScopes(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Scopes");
        Page<Scope> page = scopeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /scopes/:id} : get the "id" scope.
     *
     * @param id the id of the scope to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the scope, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Scope> getScope(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Scope : {}", id);
        Optional<Scope> scope = scopeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(scope);
    }

    /**
     * {@code DELETE  /scopes/:id} : delete the "id" scope.
     *
     * @param id the id of the scope to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScope(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Scope : {}", id);
        scopeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /scopes/_search?query=:query} : search for the scope corresponding
     * to the query.
     *
     * @param query the query of the scope search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<Scope>> searchScopes(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Scopes for query {}", query);
        try {
            Page<Scope> page = scopeService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
