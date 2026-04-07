package com.calt.buroxz.security;

import com.calt.buroxz.domain.User;
import com.calt.buroxz.repository.UserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthorizationService {

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<String> getScope() {
        User user = userRepository
            .getUserWithAuthAndScopeByUserName(getUserName())
            .orElseThrow(() -> new RuntimeException("User notfound!"));

        return user
            .getAuthorities()
            .stream()
            .flatMap(authority -> authority.getScopes().stream())
            .map(scope -> scope.getName())
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
