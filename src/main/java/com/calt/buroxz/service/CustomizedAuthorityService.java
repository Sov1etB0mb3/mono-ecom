package com.calt.buroxz.service;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.AuthorityScopeLinker;
import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.AuthorityRepository;
import com.calt.buroxz.repository.AuthorityScopeLinkerRepository;
import com.calt.buroxz.repository.CustomizedAuthorityScopeLinkerRepository;
import com.calt.buroxz.repository.ScopeRepository;
import com.calt.buroxz.service.dto.request.AuthorityRequest;
import com.calt.buroxz.service.mapper.AuthorityMapper;
import com.calt.buroxz.web.rest.AuthorityResource;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

@Service
@Transactional
public class CustomizedAuthorityService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityResource.class);

    private static final String ENTITY_NAME = "adminAuthority";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthorityRepository authorityRepository;
    private final ScopeRepository scopeRepository;
    private final AuthorityScopeLinkerRepository authLinkerRepo;
    private final CustomizedAuthorityScopeLinkerRepository customizedAuthLinkerRepo;

    public CustomizedAuthorityService(
        AuthorityRepository authorityRepository,
        ScopeRepository scopeRepository,
        AuthorityScopeLinkerRepository authLinkerRepo,
        CustomizedAuthorityScopeLinkerRepository customizedAuthLinkerRepo
    ) {
        this.authorityRepository = authorityRepository;
        this.scopeRepository = scopeRepository;
        this.authLinkerRepo = authLinkerRepo;
        this.customizedAuthLinkerRepo = customizedAuthLinkerRepo;
    }

    /**
     * {@code POST  /authorities} : Create a new authority.
     *
     * @paramauthority the authority to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authority, or with status {@code 400 (Bad Request)} if the authority has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */

    /**
     * {@code GET  /authorities} : get all the authorities.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authorities in body.
     */
    public List<Authority> getAllAuthorities() {
        LOG.debug("REST request to get all Authorities");
        return authorityRepository.findAll();
    }

    /**
     * {@code GET  /authorities/:id} : get the "id" authority.
     *
     * @param id the id of the authority to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authority, or with status {@code 404 (Not Found)}.
     */

    public ResponseEntity<Authority> getAuthority(String id) {
        LOG.debug("REST request to get Authority : {}", id);
        Optional<Authority> authority = authorityRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(authority);
    }

    public ResponseEntity<Void> updateScopeAuthority(String name, List<String> scopeList) {
        Authority authority = authorityRepository
            .findByName(name)
            .orElseThrow(() -> new RuntimeException(HttpStatus.NOT_FOUND.getReasonPhrase()));
        List<Scope> scopes = new ArrayList<>();
        for (String scopeStr : scopeList) {
            Scope scope = scopeRepository.findByName(scopeStr);
            if (scope != null) {
                scopes.add(scope);
            }
        }
        scopes
            .stream()
            .forEach(scope -> {
                AuthorityScopeLinker authorityScopeLinker = new AuthorityScopeLinker();
                authorityScopeLinker.setScope(scope);
                authorityScopeLinker.setAuthority(authority);
                if (!customizedAuthLinkerRepo.existsLinkerByAuthAndScope(authority, scope)) authLinkerRepo.save(authorityScopeLinker);
            });
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authority.getId()))
            .build();
    }

    /**
     * {@code DELETE  /authorities/:id} : delete the "id" authority.
     *
     * @param id the id of the authority to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */

    public ResponseEntity<Void> deleteAuthority(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Authority : {}", id);
        authorityRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }
}
