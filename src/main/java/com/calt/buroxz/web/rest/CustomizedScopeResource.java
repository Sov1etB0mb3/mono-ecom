//package com.calt.buroxz.web.rest;
//
//import com.calt.buroxz.domain.Scope;
//import com.calt.buroxz.repository.ScopeRepository;
//import com.calt.buroxz.security.AuthorizationService;
//import com.calt.buroxz.service.ScopeService;
//import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
//import com.calt.buroxz.web.rest.errors.ElasticsearchExceptionMapper;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//import tech.jhipster.web.util.HeaderUtil;
//import tech.jhipster.web.util.PaginationUtil;
//import tech.jhipster.web.util.ResponseUtil;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//
///**
// * REST controller for managing {@link Scope}.
// */
//
//@RestController
//public class CustomizedScopeResource extends ScopeResource{
//
//    private static final Logger LOG = LoggerFactory.getLogger(CustomizedScopeResource.class);
//
//    private static final String ENTITY_NAME = "scope";
//    private final AuthorizationService authorizationService;
//
//    @Value("${jhipster.clientApp.name}")
//    private String applicationName;
//
//    private final ScopeService scopeService;
//
//    private final ScopeRepository scopeRepository;
//
//    public CustomizedScopeResource(ScopeService scopeService, ScopeRepository scopeRepository, AuthorizationService authorizationService) {
//        super(scopeService,scopeRepository);
//        this.scopeService = scopeService;
//        this.scopeRepository = scopeRepository;
//        this.authorizationService = authorizationService;
//    }
//
//
//    @PreAuthorize("@authorizationService.hasScope('scope:all')")
//    @Override
//    public ResponseEntity<List<Scope>> getAllScopes(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
//        LOG.debug("REST request to get a page of Scopes");
//        Page<Scope> page = scopeService.findAll(pageable);
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
//        return ResponseEntity.ok().headers(headers).body(page.getContent());
//    }
//
//}
