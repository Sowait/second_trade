package com.secondtrade.market.product;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Query("select p from Product p where (:sellerId is null or p.seller.id = :sellerId)")
  Page<Product> listBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

  @Query("select p from Product p where p.status = :status and (:categoryId is null or p.category.id = :categoryId)")
  Page<Product> listByStatusAndCategory(@Param("status") ProductStatus status, @Param("categoryId") Long categoryId, Pageable pageable);

  List<Product> findByStatus(ProductStatus status);
}
