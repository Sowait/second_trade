package com.secondtrade.market.order;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<TradeOrder, Long> {
  Page<TradeOrder> findByBuyerIdOrderByIdDesc(Long buyerId, Pageable pageable);

  Page<TradeOrder> findBySellerIdOrderByIdDesc(Long sellerId, Pageable pageable);

  @Query(
    "select o from TradeOrder o " +
    "where (:status is null or o.status = :status) " +
    "and (:q is null or :q = '' or lower(o.orderNo) like lower(concat('%', :q, '%'))) " +
    "and (:start is null or o.createdAt >= :start) " +
    "and (:end is null or o.createdAt <= :end)"
  )
  Page<TradeOrder> adminQuery(
    @Param("status") OrderStatus status,
    @Param("q") String q,
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end,
    Pageable pageable
  );
}

