package com.secondtrade.market.brand;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrandRepository extends JpaRepository<Brand, Long> {
  @Query("select b from Brand b where (:cid is null or b.category.id = :cid)")
  List<Brand> listByCategory(@Param("cid") Long categoryId);
}

