package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Cart entity.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    default Optional<Cart> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Cart> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Cart> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select cart from Cart cart left join fetch cart.user", countQuery = "select count(cart) from Cart cart")
    Page<Cart> findAllWithToOneRelationships(Pageable pageable);

    @Query("select cart from Cart cart left join fetch cart.user")
    List<Cart> findAllWithToOneRelationships();

    @Query("select cart from Cart cart left join fetch cart.user where cart.id =:id")
    Optional<Cart> findOneWithToOneRelationships(@Param("id") Long id);

    Cart findCartByUserId(String userId);

    Cart findCartById(Long id);
}
