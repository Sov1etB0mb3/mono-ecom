package com.calt.buroxz.domain;

import static com.calt.buroxz.domain.AuthorityScopeLinkerTestSamples.*;
import static com.calt.buroxz.domain.ScopeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.calt.buroxz.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuthorityScopeLinkerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuthorityScopeLinker.class);
        AuthorityScopeLinker authorityScopeLinker1 = getAuthorityScopeLinkerSample1();
        AuthorityScopeLinker authorityScopeLinker2 = new AuthorityScopeLinker();
        assertThat(authorityScopeLinker1).isNotEqualTo(authorityScopeLinker2);

        authorityScopeLinker2.setId(authorityScopeLinker1.getId());
        assertThat(authorityScopeLinker1).isEqualTo(authorityScopeLinker2);

        authorityScopeLinker2 = getAuthorityScopeLinkerSample2();
        assertThat(authorityScopeLinker1).isNotEqualTo(authorityScopeLinker2);
    }

    @Test
    void scopeTest() {
        AuthorityScopeLinker authorityScopeLinker = getAuthorityScopeLinkerRandomSampleGenerator();
        Scope scopeBack = getScopeRandomSampleGenerator();

        authorityScopeLinker.setScope(scopeBack);
        assertThat(authorityScopeLinker.getScope()).isEqualTo(scopeBack);

        authorityScopeLinker.scope(null);
        assertThat(authorityScopeLinker.getScope()).isNull();
    }
}
