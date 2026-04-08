package com.calt.buroxz.web.rest;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.repository.AuthorityRepository;
import com.calt.buroxz.service.AuthorityService;
import com.calt.buroxz.service.CustomizedAuthorityService;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link Authority}.
 */
@RestController
@RequestMapping("/api/authorities")
@Transactional
public class CustomizedAuthorityResource {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedAuthorityResource.class);

    private static final String ENTITY_NAME = "adminAuthority";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CustomizedAuthorityService customizedAuthorityService;

    public CustomizedAuthorityResource(CustomizedAuthorityService customizedAuthorityService) {
        this.customizedAuthorityService = customizedAuthorityService;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updateScope(@PathVariable("id") String id, @RequestBody List<String> scopeList) {
        LOG.debug("REST request to update scopes for authority: ID: {}|SCOPES: {}", id, scopeList);
        return customizedAuthorityService.updateScopeAuthority(id, scopeList);
    }
}
