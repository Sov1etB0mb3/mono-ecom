package com.calt.buroxz.repository;

import com.calt.buroxz.domain.AuthorityScopeLinker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AuthorityScopeLinker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorityScopeLinkerRepository extends JpaRepository<AuthorityScopeLinker, Long> {
    @Query("SELECT asl FROM AuthorityScopeLinker asl LEFT JOIN FETCH asl.authority LEFT JOIN FETCH asl.scope")
    List<AuthorityScopeLinker> findAll();

    @Query("SELECT asl FROM AuthorityScopeLinker asl LEFT JOIN FETCH asl.authority LEFT JOIN FETCH asl.scope WHERE asl.id = :id")
    Optional<AuthorityScopeLinker> findById(@Param("id") Long id);
}
