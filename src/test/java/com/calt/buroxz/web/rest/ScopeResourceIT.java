package com.calt.buroxz.web.rest;

import static com.calt.buroxz.domain.ScopeAsserts.*;
import static com.calt.buroxz.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.calt.buroxz.IntegrationTest;
import com.calt.buroxz.domain.Scope;
import com.calt.buroxz.repository.ScopeRepository;
import com.calt.buroxz.repository.search.ScopeSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ScopeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ScopeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/scopes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/scopes/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ScopeRepository scopeRepository;

    @Autowired
    private ScopeSearchRepository scopeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restScopeMockMvc;

    private Scope scope;

    private Scope insertedScope;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Scope createEntity() {
        return new Scope().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Scope createUpdatedEntity() {
        return new Scope().name(UPDATED_NAME).description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        scope = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedScope != null) {
            scopeRepository.delete(insertedScope);
            scopeSearchRepository.delete(insertedScope);
            insertedScope = null;
        }
    }

    @Test
    @Transactional
    void createScope() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        // Create the Scope
        var returnedScope = om.readValue(
            restScopeMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(scope)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Scope.class
        );

        // Validate the Scope in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertScopeUpdatableFieldsEquals(returnedScope, getPersistedScope(returnedScope));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedScope = returnedScope;
    }

    @Test
    @Transactional
    void createScopeWithExistingId() throws Exception {
        // Create the Scope with an existing ID
        scope.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restScopeMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(scope)))
            .andExpect(status().isBadRequest());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        // set the field null
        scope.setName(null);

        // Create the Scope, which fails.

        restScopeMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(scope)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllScopes() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);

        // Get all the scopeList
        restScopeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(scope.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getScope() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);

        // Get the scope
        restScopeMockMvc
            .perform(get(ENTITY_API_URL_ID, scope.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(scope.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingScope() throws Exception {
        // Get the scope
        restScopeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingScope() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        scopeSearchRepository.save(scope);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());

        // Update the scope
        Scope updatedScope = scopeRepository.findById(scope.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedScope are not directly saved in db
        em.detach(updatedScope);
        updatedScope.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restScopeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedScope.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedScope))
            )
            .andExpect(status().isOk());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedScopeToMatchAllProperties(updatedScope);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Scope> scopeSearchList = Streamable.of(scopeSearchRepository.findAll()).toList();
                Scope testScopeSearch = scopeSearchList.get(searchDatabaseSizeAfter - 1);

                assertScopeAllPropertiesEquals(testScopeSearch, updatedScope);
            });
    }

    @Test
    @Transactional
    void putNonExistingScope() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        scope.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restScopeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, scope.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(scope))
            )
            .andExpect(status().isBadRequest());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchScope() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        scope.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restScopeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(scope))
            )
            .andExpect(status().isBadRequest());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamScope() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        scope.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restScopeMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(scope)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateScopeWithPatch() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the scope using partial update
        Scope partialUpdatedScope = new Scope();
        partialUpdatedScope.setId(scope.getId());

        restScopeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedScope.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedScope))
            )
            .andExpect(status().isOk());

        // Validate the Scope in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertScopeUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedScope, scope), getPersistedScope(scope));
    }

    @Test
    @Transactional
    void fullUpdateScopeWithPatch() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the scope using partial update
        Scope partialUpdatedScope = new Scope();
        partialUpdatedScope.setId(scope.getId());

        partialUpdatedScope.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restScopeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedScope.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedScope))
            )
            .andExpect(status().isOk());

        // Validate the Scope in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertScopeUpdatableFieldsEquals(partialUpdatedScope, getPersistedScope(partialUpdatedScope));
    }

    @Test
    @Transactional
    void patchNonExistingScope() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        scope.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restScopeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, scope.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(scope))
            )
            .andExpect(status().isBadRequest());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchScope() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        scope.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restScopeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(scope))
            )
            .andExpect(status().isBadRequest());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamScope() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        scope.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restScopeMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(scope)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Scope in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteScope() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);
        scopeRepository.save(scope);
        scopeSearchRepository.save(scope);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the scope
        restScopeMockMvc
            .perform(delete(ENTITY_API_URL_ID, scope.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(scopeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchScope() throws Exception {
        // Initialize the database
        insertedScope = scopeRepository.saveAndFlush(scope);
        scopeSearchRepository.save(scope);

        // Search the scope
        restScopeMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + scope.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(scope.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    protected long getRepositoryCount() {
        return scopeRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Scope getPersistedScope(Scope scope) {
        return scopeRepository.findById(scope.getId()).orElseThrow();
    }

    protected void assertPersistedScopeToMatchAllProperties(Scope expectedScope) {
        assertScopeAllPropertiesEquals(expectedScope, getPersistedScope(expectedScope));
    }

    protected void assertPersistedScopeToMatchUpdatableProperties(Scope expectedScope) {
        assertScopeAllUpdatablePropertiesEquals(expectedScope, getPersistedScope(expectedScope));
    }
}
