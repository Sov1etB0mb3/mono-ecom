//package com.calt.buroxz.domain;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import java.io.Serializable;
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Set;
//import org.hibernate.annotations.BatchSize;
//import org.hibernate.annotations.Cache;
//import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.springframework.data.domain.Persistable;
//
///**
// * A Authority.
// */
//@Entity
//@Table(name = "jhi_authority")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@JsonIgnoreProperties(value = { "new", "id" })
//@SuppressWarnings("common-java:DuplicatedBlocks")
//public class CustomizedAuthority implements Serializable, Persistable<String> {
//
//    private static final long serialVersionUID = 1L;
//
//    @NotNull
//    @Size(max = 50)
//    @Id
//    @Column(name = "name", length = 50, nullable = false)
//    private String name;
//
//    @org.springframework.data.annotation.Transient
//    @Transient
//    private boolean isPersisted;
//
//    @JsonIgnore
//    @ManyToMany
//    @JoinTable(
//        name = "jhi_authority_scope",
//        joinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") },
//        inverseJoinColumns = { @JoinColumn(name = "scope_id", referencedColumnName = "id") }
//    )
//    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    @BatchSize(size = 20)
//    private Set<Scope> scopes = new HashSet<>();
//
//    // jhipster-needle-entity-add-field - JHipster will add fields here
//
//    public Set<Scope> getScopes() {
//        return scopes;
//    }
//
//    public void setScopes(Set<Scope> scopes) {
//        this.scopes = scopes;
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public CustomizedAuthority name(String name) {
//        this.setName(name);
//        return this;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    @PostLoad
//    @PostPersist
//    public void updateEntityState() {
//        this.setIsPersisted();
//    }
//
//    @Override
//    public String getId() {
//        return this.name;
//    }
//
//    @org.springframework.data.annotation.Transient
//    @Transient
//    @Override
//    public boolean isNew() {
//        return !this.isPersisted;
//    }
//
//    public CustomizedAuthority setIsPersisted() {
//        this.isPersisted = true;
//        return this;
//    }
//
//    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (!(o instanceof CustomizedAuthority)) {
//            return false;
//        }
//        return getName() != null && getName().equals(((CustomizedAuthority) o).getName());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hashCode(getName());
//    }
//
//    // prettier-ignore
//    @Override
//    public String toString() {
//        return "Authority{" +
//            "name=" + getName() +
//            "}";
//    }
//}
