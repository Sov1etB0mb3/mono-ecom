package com.calt.buroxz.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.PREFERRED_USERNAME;

import com.calt.buroxz.domain.Authority;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.repository.AuthorityRepository;
import com.calt.buroxz.repository.UserRepository;
import com.calt.buroxz.security.AuthoritiesConstants;
import com.calt.buroxz.security.SecurityUtils;
import com.calt.buroxz.security.oauth2.AudienceValidator;
import com.calt.buroxz.security.oauth2.CustomClaimConverter;
import com.calt.buroxz.service.UserService;
import com.calt.buroxz.web.filter.SpaWebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import tech.jhipster.config.JHipsterProperties;

@Slf4j
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class CustomizedSecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final AuthorityRepository authorityRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    public CustomizedSecurityConfiguration(
        JHipsterProperties jHipsterProperties,
        AuthorityRepository authorityRepository,
        UserService userService,
        UserRepository userRepository
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.authorityRepository = authorityRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf ->
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )
            .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
            .headers(headers ->
                headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
            )
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                    .requestMatchers(mvc.pattern("/index.html"), mvc.pattern("/*.js"), mvc.pattern("/*.txt"), mvc.pattern("/*.json"), mvc.pattern("/*.map"), mvc.pattern("/*.css")).permitAll()
                    .requestMatchers(mvc.pattern("/*.ico"), mvc.pattern("/*.png"), mvc.pattern("/*.svg"), mvc.pattern("/*.webapp")).permitAll()
                    .requestMatchers(mvc.pattern("/app/**")).permitAll()
                    .requestMatchers(mvc.pattern("/i18n/**")).permitAll()
                    .requestMatchers(mvc.pattern("/content/**")).permitAll()
                    .requestMatchers(mvc.pattern("/swagger-ui/**")).permitAll()
                    .requestMatchers(mvc.pattern("/api/authenticate")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth-info")).permitAll()
                    .requestMatchers(mvc.pattern("/api/admin/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(mvc.pattern("/api/**")).authenticated()
                    .requestMatchers(mvc.pattern("/v3/api-docs/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(mvc.pattern("/management/health")).permitAll()
                    .requestMatchers(mvc.pattern("/management/health/**")).permitAll()
                    .requestMatchers(mvc.pattern("/management/info")).permitAll()
                    .requestMatchers(mvc.pattern("/management/prometheus")).permitAll()
                    .requestMatchers(mvc.pattern("/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .oauth2Login(oauth2 -> oauth2.loginPage("/").userInfoEndpoint(userInfo -> userInfo.oidcUserService(this.oidcUserService())))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customziedAuthenticationConverter())))
            .oauth2Client(withDefaults());
        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            new Converter<Jwt, Collection<GrantedAuthority>>() {
                @Override
                public Collection<GrantedAuthority> convert(Jwt jwt) {
                    return SecurityUtils.extractAuthorityFromClaims(jwt.getClaims());
                }
            }
        );
        jwtAuthenticationConverter.setPrincipalClaimName(PREFERRED_USERNAME);
        return jwtAuthenticationConverter;
    }

    Converter<Jwt, AbstractAuthenticationToken> customziedAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            new Converter<Jwt, Collection<GrantedAuthority>>() {
                @Override
                public Collection<GrantedAuthority> convert(Jwt jwt) {
                    String id = jwt.getSubject();
                    log.debug("currentUser: " + id);
                    User user = userRepository.getUserWithAuthAndScope(id).orElseThrow(() -> new RuntimeException("User notfound " + id));
                    log.debug("User authorities: " + user.getAuthorities());
                    user
                        .getAuthorities()
                        .forEach(authority -> {
                            log.debug("Authority: " + authority.getName());
                            log.debug("Scopes: " + authority.getScopes());
                        });
                    List<Authority> authorityList = user.getAuthorities().stream().toList();
                    Set<GrantedAuthority> scopeList = authorityList
                        .stream()
                        .flatMap(authority -> authority.getScopes().stream().map(scope -> new SimpleGrantedAuthority(scope.getName())))
                        .collect(Collectors.toSet());
                    List<GrantedAuthority> grantedAuthorities = new ArrayList<>(SecurityUtils.extractAuthorityFromClaims(jwt.getClaims()));
                    log.debug("After:" + grantedAuthorities.toString());
                    grantedAuthorities.addAll(scopeList);

                    return grantedAuthorities;
                }
            }
        );
        jwtAuthenticationConverter.setPrincipalClaimName(PREFERRED_USERNAME);
        return jwtAuthenticationConverter;
    }

    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(), PREFERRED_USERNAME);
        };
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a {@link GrantedAuthoritiesMapper} that maps groups from
     * the IdP to Spring Security Authorities.
     */
    //This is where SecurityContextHolder take authorities, not from converter anymore!
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        //        return authorities -> {
        //            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        //
        //            authorities.forEach(authority -> {
        //                // Check for OidcUserAuthority because Spring Security 5.2 returns
        //                // each scope as a GrantedAuthority, which we don't care about.
        //                if (authority instanceof OidcUserAuthority) {
        //                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
        //                    mappedAuthorities.addAll(SecurityUtils.extractAuthorityFromClaims(oidcUserAuthority.getUserInfo().getClaims()));
        //                }
        //            });
        //            return mappedAuthorities;
        //        };
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;

                    System.out.println("========================================");
                    System.out.println("USER AUTHORITIES MAPPER CALLED!!!!");
                    System.out.println("========================================");

                    // 1. Add standard roles from claims
                    mappedAuthorities.addAll(SecurityUtils.extractAuthorityFromClaims(oidcUserAuthority.getUserInfo().getClaims()));

                    // 2. Add scopes from database
                    String id = oidcUserAuthority.getIdToken().getSubject();
                    userRepository
                        .getUserWithAuthAndScope(id)
                        .ifPresent(user -> {
                            user
                                .getAuthorities()
                                .forEach(auth -> {
                                    System.out.println("Authority: " + auth.getName());
                                    auth
                                        .getScopes()
                                        .forEach(scope -> {
                                            System.out.println("  Adding scope: " + scope.getName());
                                            mappedAuthorities.add(new SimpleGrantedAuthority(scope.getName()));
                                        });
                                });
                        });

                    System.out.println("Final mapped authorities: " + mappedAuthorities);
                } else {
                    // Keep other authorities as-is
                    mappedAuthorities.add(authority);
                }
            });

            return mappedAuthorities;
        };
    }

    @Bean
    JwtDecoder jwtDecoder(ClientRegistrationRepository clientRegistrationRepository, RestTemplateBuilder restTemplateBuilder) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(jHipsterProperties.getSecurity().getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        jwtDecoder.setClaimSetConverter(
            new CustomClaimConverter(clientRegistrationRepository.findByRegistrationId("oidc"), restTemplateBuilder.build())
        );

        return jwtDecoder;
    }

    /**
     * Custom CSRF handler to provide BREACH protection for Single-Page Applications (SPA).
     *
     * @see <a href="https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa">Spring Security Documentation - Integrating with CSRF Protection</a>
     * @see <a href="https://github.com/jhipster/generator-jhipster/pull/25907">JHipster - use customized SpaCsrfTokenRequestHandler to handle CSRF token</a>
     * @see <a href="https://stackoverflow.com/q/74447118/65681">CSRF protection not working with Spring Security 6</a>
     */
    static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            /*
             * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
             * the CsrfToken when it is rendered in the response body.
             */
            this.xor.handle(request, response, csrfToken);

            // Render the token value to a cookie by causing the deferred token to be loaded.
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            /*
             * If the request contains a request header, use CsrfTokenRequestAttributeHandler
             * to resolve the CsrfToken. This applies when a single-page application includes
             * the header value automatically, which was obtained via a cookie containing the
             * raw CsrfToken.
             */
            if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                return this.plain.resolveCsrfTokenValue(request, csrfToken);
            }
            /*
             * In all other cases (e.g. if the request contains a request parameter), use
             * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
             * when a server-side rendered form includes the _csrf request parameter as a
             * hidden input.
             */
            return this.xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
