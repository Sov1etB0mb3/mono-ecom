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

    public Authority requestToAuthority(AuthorityRequest authorityRequest) {
        Authority authority = new Authority();
        Set<Scope> scopes = new HashSet<>();
        scopes = authorityRequest
            .getScopes()
            .stream()
            .map(scope -> {
                Scope fscope = scopeRepository.findByName(scope);
                return fscope;
            })
            .collect(Collectors.toSet());
        authority.setName(authorityRequest.getName());
        authority.setScopes(scopes);
        return authority;
    }
}
