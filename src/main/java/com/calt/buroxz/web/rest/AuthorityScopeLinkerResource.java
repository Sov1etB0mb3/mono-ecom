package com.calt.buroxz.web.rest;

import com.calt.buroxz.domain.AuthorityScopeLinker;
import com.calt.buroxz.repository.AuthorityScopeLinkerRepository;
import com.calt.buroxz.repository.search.AuthorityScopeLinkerSearchRepository;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import com.calt.buroxz.web.rest.errors.ElasticsearchExceptionMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.calt.buroxz.domain.AuthorityScopeLinker}.
 */
@RestController
@RequestMapping("/api/authority-scope-linkers")
@Transactional
public class AuthorityScopeLinkerResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityScopeLinkerResource.class);

    private static final String ENTITY_NAME = "authorityScopeLinker";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthorityScopeLinkerRepository authorityScopeLinkerRepository;

    private final AuthorityScopeLinkerSearchRepository authorityScopeLinkerSearchRepository;

    public AuthorityScopeLinkerResource(
        AuthorityScopeLinkerRepository authorityScopeLinkerRepository,
        AuthorityScopeLinkerSearchRepository authorityScopeLinkerSearchRepository
    ) {
        this.authorityScopeLinkerRepository = authorityScopeLinkerRepository;
        this.authorityScopeLinkerSearchRepository = authorityScopeLinkerSearchRepository;
    }

    /**
     * {@code POST  /authority-scope-linkers} : Create a new authorityScopeLinker.
     *
     * @param authorityScopeLinker the authorityScopeLinker to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authorityScopeLinker, or with status {@code 400 (Bad Request)} if the authorityScopeLinker has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AuthorityScopeLinker> createAuthorityScopeLinker(@RequestBody AuthorityScopeLinker authorityScopeLinker)
        throws URISyntaxException {
        LOG.debug("REST request to save AuthorityScopeLinker : {}", authorityScopeLinker);
        if (authorityScopeLinker.getId() != null) {
            throw new BadRequestAlertException("A new authorityScopeLinker cannot already have an ID", ENTITY_NAME, "idexists");
        }
        authorityScopeLinker = authorityScopeLinkerRepository.save(authorityScopeLinker);
        authorityScopeLinkerSearchRepository.index(authorityScopeLinker);
        return ResponseEntity.created(new URI("/api/authority-scope-linkers/" + authorityScopeLinker.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, authorityScopeLinker.getId().toString()))
            .body(authorityScopeLinker);
    }

    /**
     * {@code PUT  /authority-scope-linkers/:id} : Updates an existing authorityScopeLinker.
     *
     * @param id the id of the authorityScopeLinker to save.
     * @param authorityScopeLinker the authorityScopeLinker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authorityScopeLinker,
     * or with status {@code 400 (Bad Request)} if the authorityScopeLinker is not valid,
     * or with status {@code 500 (Internal Server Error)} if the authorityScopeLinker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuthorityScopeLinker> updateAuthorityScopeLinker(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AuthorityScopeLinker authorityScopeLinker
    ) throws URISyntaxException {
        LOG.debug("REST request to update AuthorityScopeLinker : {}, {}", id, authorityScopeLinker);
        if (authorityScopeLinker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, authorityScopeLinker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!authorityScopeLinkerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        authorityScopeLinker = authorityScopeLinkerRepository.save(authorityScopeLinker);
        authorityScopeLinkerSearchRepository.index(authorityScopeLinker);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authorityScopeLinker.getId().toString()))
            .body(authorityScopeLinker);
    }

    /**
     * {@code PATCH  /authority-scope-linkers/:id} : Partial updates given fields of an existing authorityScopeLinker, field will ignore if it is null
     *
     * @param id the id of the authorityScopeLinker to save.
     * @param authorityScopeLinker the authorityScopeLinker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authorityScopeLinker,
     * or with status {@code 400 (Bad Request)} if the authorityScopeLinker is not valid,
     * or with status {@code 404 (Not Found)} if the authorityScopeLinker is not found,
     * or with status {@code 500 (Internal Server Error)} if the authorityScopeLinker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AuthorityScopeLinker> partialUpdateAuthorityScopeLinker(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AuthorityScopeLinker authorityScopeLinker
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AuthorityScopeLinker partially : {}, {}", id, authorityScopeLinker);
        if (authorityScopeLinker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, authorityScopeLinker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!authorityScopeLinkerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AuthorityScopeLinker> result = authorityScopeLinkerRepository
            .findById(authorityScopeLinker.getId())
            .map(authorityScopeLinkerRepository::save)
            .map(savedAuthorityScopeLinker -> {
                authorityScopeLinkerSearchRepository.index(savedAuthorityScopeLinker);
                return savedAuthorityScopeLinker;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authorityScopeLinker.getId().toString())
        );
    }

    /**
     * {@code GET  /authority-scope-linkers} : get all the authorityScopeLinkers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authorityScopeLinkers in body.
     */
    @GetMapping("")
    public List<AuthorityScopeLinker> getAllAuthorityScopeLinkers() {
        LOG.debug("REST request to get all AuthorityScopeLinkers");
        return authorityScopeLinkerRepository.findAll();
    }

    /**
     * {@code GET  /authority-scope-linkers/:id} : get the "id" authorityScopeLinker.
     *
     * @param id the id of the authorityScopeLinker to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authorityScopeLinker, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthorityScopeLinker> getAuthorityScopeLinker(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AuthorityScopeLinker : {}", id);
        Optional<AuthorityScopeLinker> authorityScopeLinker = authorityScopeLinkerRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(authorityScopeLinker);
    }

    /**
     * {@code DELETE  /authority-scope-linkers/:id} : delete the "id" authorityScopeLinker.
     *
     * @param id the id of the authorityScopeLinker to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthorityScopeLinker(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AuthorityScopeLinker : {}", id);
        authorityScopeLinkerRepository.deleteById(id);
        authorityScopeLinkerSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /authority-scope-linkers/_search?query=:query} : search for the authorityScopeLinker corresponding
     * to the query.
     *
     * @param query the query of the authorityScopeLinker search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<AuthorityScopeLinker> searchAuthorityScopeLinkers(@RequestParam("query") String query) {
        LOG.debug("REST request to search AuthorityScopeLinkers for query {}", query);
        try {
            return StreamSupport.stream(authorityScopeLinkerSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
