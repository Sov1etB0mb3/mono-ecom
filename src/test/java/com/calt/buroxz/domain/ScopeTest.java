package com.calt.buroxz.domain;

import static com.calt.buroxz.domain.ScopeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.calt.buroxz.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ScopeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Scope.class);
        Scope scope1 = getScopeSample1();
        Scope scope2 = new Scope();
        assertThat(scope1).isNotEqualTo(scope2);

        scope2.setId(scope1.getId());
        assertThat(scope1).isEqualTo(scope2);

        scope2 = getScopeSample2();
        assertThat(scope1).isNotEqualTo(scope2);
    }
}
