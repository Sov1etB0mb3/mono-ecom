package com.calt.buroxz.security;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.AuthorityScopeLinker;
import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.repository.AuthorityScopeLinkerRepository;
import com.calt.buroxz.repository.CustomizedAuthorityScopeLinkerRepository;
import com.calt.buroxz.repository.CustomizedUserRepository;
import com.calt.buroxz.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthorizationService {

    private final CustomizedUserRepository userRepository;
    private final AuthorityScopeLinkerRepository authLinkerRepository;
    private final CustomizedAuthorityScopeLinkerRepository customizedAuthLinkerRepo;

    public AuthorizationService(
        CustomizedUserRepository userRepository,
        AuthorityScopeLinkerRepository authLinkerRepository,
        CustomizedAuthorityScopeLinkerRepository customizedAuthLinkerRepo
    ) {
        this.userRepository = userRepository;
        this.authLinkerRepository = authLinkerRepository;
        this.customizedAuthLinkerRepo = customizedAuthLinkerRepo;
    }

    public Set<String> getScope() {
        User user = userRepository.getUserWithAuthByUserName(getUserName()).orElseThrow(() -> new RuntimeException("User notfound!"));
        Set<String> authorities = user.getAuthorities().stream().map(a -> a.getName()).collect(Collectors.toSet());

        return customizedAuthLinkerRepo
            .findAuthLinkerWithScopes(authorities)
            .stream()
            .map(linker -> linker.getScope().getName())
            .collect(Collectors.toSet());
    }

    public boolean hasScope(String scope) {
        Set<String> scopeSet = getScope();
        return scopeSet.contains(scope);
    }

    public String getUserName() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        log.info("CURRENT Name: " + userName);
        return userName;
    }
}
