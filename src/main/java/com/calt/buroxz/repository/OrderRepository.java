package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Order entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select jhiOrder from Order jhiOrder where jhiOrder.user.login = ?#{authentication.name}")
    List<Order> findByUserIsCurrentUser();
}
