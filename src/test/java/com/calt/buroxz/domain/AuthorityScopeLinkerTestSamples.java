package com.calt.buroxz.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class AuthorityScopeLinkerTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AuthorityScopeLinker getAuthorityScopeLinkerSample1() {
        return new AuthorityScopeLinker().id(1L);
    }

    public static AuthorityScopeLinker getAuthorityScopeLinkerSample2() {
        return new AuthorityScopeLinker().id(2L);
    }

    public static AuthorityScopeLinker getAuthorityScopeLinkerRandomSampleGenerator() {
        return new AuthorityScopeLinker().id(longCount.incrementAndGet());
    }
}
