package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.ScopeRepository;
import com.calt.buroxz.service.dto.request.AuthorityRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuthorityMapper {

    private ScopeRepository scopeRepository;

    public AuthorityMapper(ScopeRepository scopeRepository) {
        this.scopeRepository = scopeRepository;
    }
}
