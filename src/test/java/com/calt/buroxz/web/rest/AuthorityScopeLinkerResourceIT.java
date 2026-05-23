package com.calt.buroxz.web.rest;

import static com.calt.buroxz.domain.AuthorityScopeLinkerAsserts.*;
import static com.calt.buroxz.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.calt.buroxz.IntegrationTest;
import com.calt.buroxz.domain.AuthorityScopeLinker;
import com.calt.buroxz.repository.AuthorityScopeLinkerRepository;
import com.calt.buroxz.repository.search.AuthorityScopeLinkerSearchRepository;
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
 * Integration tests for the {@link AuthorityScopeLinkerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AuthorityScopeLinkerResourceIT {

    private static final String ENTITY_API_URL = "/api/authority-scope-linkers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/authority-scope-linkers/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuthorityScopeLinkerRepository authorityScopeLinkerRepository;

    @Autowired
    private AuthorityScopeLinkerSearchRepository authorityScopeLinkerSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuthorityScopeLinkerMockMvc;

    private AuthorityScopeLinker authorityScopeLinker;

    private AuthorityScopeLinker insertedAuthorityScopeLinker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuthorityScopeLinker createEntity() {
        return new AuthorityScopeLinker();
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuthorityScopeLinker createUpdatedEntity() {
        return new AuthorityScopeLinker();
    }

    @BeforeEach
    void initTest() {
        authorityScopeLinker = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAuthorityScopeLinker != null) {
            authorityScopeLinkerRepository.delete(insertedAuthorityScopeLinker);
            authorityScopeLinkerSearchRepository.delete(insertedAuthorityScopeLinker);
            insertedAuthorityScopeLinker = null;
        }
    }

    @Test
    @Transactional
    void createAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        // Create the AuthorityScopeLinker
        var returnedAuthorityScopeLinker = om.readValue(
            restAuthorityScopeLinkerMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(authorityScopeLinker))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AuthorityScopeLinker.class
        );

        // Validate the AuthorityScopeLinker in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertAuthorityScopeLinkerUpdatableFieldsEquals(
            returnedAuthorityScopeLinker,
            getPersistedAuthorityScopeLinker(returnedAuthorityScopeLinker)
        );

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedAuthorityScopeLinker = returnedAuthorityScopeLinker;
    }

    @Test
    @Transactional
    void createAuthorityScopeLinkerWithExistingId() throws Exception {
        // Create the AuthorityScopeLinker with an existing ID
        authorityScopeLinker.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorityScopeLinkerMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllAuthorityScopeLinkers() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);

        // Get all the authorityScopeLinkerList
        restAuthorityScopeLinkerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(authorityScopeLinker.getId().intValue())));
    }

    @Test
    @Transactional
    void getAuthorityScopeLinker() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);

        // Get the authorityScopeLinker
        restAuthorityScopeLinkerMockMvc
            .perform(get(ENTITY_API_URL_ID, authorityScopeLinker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(authorityScopeLinker.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingAuthorityScopeLinker() throws Exception {
        // Get the authorityScopeLinker
        restAuthorityScopeLinkerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAuthorityScopeLinker() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        authorityScopeLinkerSearchRepository.save(authorityScopeLinker);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());

        // Update the authorityScopeLinker
        AuthorityScopeLinker updatedAuthorityScopeLinker = authorityScopeLinkerRepository
            .findById(authorityScopeLinker.getId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedAuthorityScopeLinker are not directly saved in db
        em.detach(updatedAuthorityScopeLinker);

        restAuthorityScopeLinkerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAuthorityScopeLinker.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedAuthorityScopeLinker))
            )
            .andExpect(status().isOk());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAuthorityScopeLinkerToMatchAllProperties(updatedAuthorityScopeLinker);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AuthorityScopeLinker> authorityScopeLinkerSearchList = Streamable.of(
                    authorityScopeLinkerSearchRepository.findAll()
                ).toList();
                AuthorityScopeLinker testAuthorityScopeLinkerSearch = authorityScopeLinkerSearchList.get(searchDatabaseSizeAfter - 1);

                assertAuthorityScopeLinkerAllPropertiesEquals(testAuthorityScopeLinkerSearch, updatedAuthorityScopeLinker);
            });
    }

    @Test
    @Transactional
    void putNonExistingAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        authorityScopeLinker.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorityScopeLinkerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, authorityScopeLinker.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        authorityScopeLinker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorityScopeLinkerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        authorityScopeLinker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorityScopeLinkerMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateAuthorityScopeLinkerWithPatch() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the authorityScopeLinker using partial update
        AuthorityScopeLinker partialUpdatedAuthorityScopeLinker = new AuthorityScopeLinker();
        partialUpdatedAuthorityScopeLinker.setId(authorityScopeLinker.getId());

        restAuthorityScopeLinkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthorityScopeLinker.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuthorityScopeLinker))
            )
            .andExpect(status().isOk());

        // Validate the AuthorityScopeLinker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuthorityScopeLinkerUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAuthorityScopeLinker, authorityScopeLinker),
            getPersistedAuthorityScopeLinker(authorityScopeLinker)
        );
    }

    @Test
    @Transactional
    void fullUpdateAuthorityScopeLinkerWithPatch() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the authorityScopeLinker using partial update
        AuthorityScopeLinker partialUpdatedAuthorityScopeLinker = new AuthorityScopeLinker();
        partialUpdatedAuthorityScopeLinker.setId(authorityScopeLinker.getId());

        restAuthorityScopeLinkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthorityScopeLinker.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuthorityScopeLinker))
            )
            .andExpect(status().isOk());

        // Validate the AuthorityScopeLinker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuthorityScopeLinkerUpdatableFieldsEquals(
            partialUpdatedAuthorityScopeLinker,
            getPersistedAuthorityScopeLinker(partialUpdatedAuthorityScopeLinker)
        );
    }

    @Test
    @Transactional
    void patchNonExistingAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        authorityScopeLinker.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorityScopeLinkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, authorityScopeLinker.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        authorityScopeLinker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorityScopeLinkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuthorityScopeLinker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        authorityScopeLinker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorityScopeLinkerMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(authorityScopeLinker))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AuthorityScopeLinker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteAuthorityScopeLinker() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);
        authorityScopeLinkerRepository.save(authorityScopeLinker);
        authorityScopeLinkerSearchRepository.save(authorityScopeLinker);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the authorityScopeLinker
        restAuthorityScopeLinkerMockMvc
            .perform(delete(ENTITY_API_URL_ID, authorityScopeLinker.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorityScopeLinkerSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchAuthorityScopeLinker() throws Exception {
        // Initialize the database
        insertedAuthorityScopeLinker = authorityScopeLinkerRepository.saveAndFlush(authorityScopeLinker);
        authorityScopeLinkerSearchRepository.save(authorityScopeLinker);

        // Search the authorityScopeLinker
        restAuthorityScopeLinkerMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + authorityScopeLinker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(authorityScopeLinker.getId().intValue())));
    }

    protected long getRepositoryCount() {
        return authorityScopeLinkerRepository.count();
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

    protected AuthorityScopeLinker getPersistedAuthorityScopeLinker(AuthorityScopeLinker authorityScopeLinker) {
        return authorityScopeLinkerRepository.findById(authorityScopeLinker.getId()).orElseThrow();
    }

    protected void assertPersistedAuthorityScopeLinkerToMatchAllProperties(AuthorityScopeLinker expectedAuthorityScopeLinker) {
        assertAuthorityScopeLinkerAllPropertiesEquals(
            expectedAuthorityScopeLinker,
            getPersistedAuthorityScopeLinker(expectedAuthorityScopeLinker)
        );
    }

    protected void assertPersistedAuthorityScopeLinkerToMatchUpdatableProperties(AuthorityScopeLinker expectedAuthorityScopeLinker) {
        assertAuthorityScopeLinkerAllUpdatablePropertiesEquals(
            expectedAuthorityScopeLinker,
            getPersistedAuthorityScopeLinker(expectedAuthorityScopeLinker)
        );
    }
}
