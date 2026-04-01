package com.calt.buroxz;

import com.calt.buroxz.config.AsyncSyncConfiguration;
import com.calt.buroxz.config.EmbeddedElasticsearch;
import com.calt.buroxz.config.EmbeddedRedis;
import com.calt.buroxz.config.EmbeddedSQL;
import com.calt.buroxz.config.JacksonConfiguration;
import com.calt.buroxz.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { MonoEcomApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class })
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
public @interface IntegrationTest {
}
