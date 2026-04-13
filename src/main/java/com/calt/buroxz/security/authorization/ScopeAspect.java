package com.calt.buroxz.security.authorization;

import com.calt.buroxz.security.AuthorizationService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ScopeAspect {

    private final AuthorizationService authorizationService;

    public ScopeAspect(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Before("execution(* com.calt.buroxz.service.*Service.*(..))" + "&& !target(com.calt.buroxz.service.UserService)")
    public void check(JoinPoint joinPoint) {
        String scope = resolveScope(joinPoint);
        boolean isAdmin = authorizationService.hasScope("scope:all");
        if (!authorizationService.hasScope(scope) && !isAdmin) throw new RuntimeException("Forbidden: " + scope);
    }

    private String resolveScope(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String entity = className.replaceAll("Customized", "").toLowerCase().replaceAll("(?i)Service", "").toLowerCase();
        String action = mapAction(methodName);

        return entity + ":" + action;
    }

    private String mapAction(String methodName) {
        if (methodName.startsWith("save")) return "create";
        if (methodName.startsWith("update")) return "update";
        if (methodName.startsWith("find") || methodName.startsWith("get")) return "read";
        if (methodName.startsWith("delete")) return "delete";
        return "unknown";
    }
}
