package com.calt.buroxz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AuthorityScopeLinker.
 */
@Entity
@Table(name = "authority_scope_linker")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "authorityscopelinker")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuthorityScopeLinker implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Authority authority;

    @ManyToOne(fetch = FetchType.LAZY)
    private Scope scope;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuthorityScopeLinker id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Authority getAuthority() {
        return this.authority;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public AuthorityScopeLinker authority(Authority authority) {
        this.setAuthority(authority);
        return this;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public AuthorityScopeLinker scope(Scope scope) {
        this.setScope(scope);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorityScopeLinker)) {
            return false;
        }
        return getId() != null && getId().equals(((AuthorityScopeLinker) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuthorityScopeLinker{" +
            "id=" + getId() +
            "}";
    }
}
