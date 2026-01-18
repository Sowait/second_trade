package com.secondtrade.market.device;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceModelRepository extends JpaRepository<DeviceModel, Long> {
  @Query("select m from DeviceModel m where (:cid is null or m.category.id = :cid) and (:bid is null or m.brand.id = :bid)")
  List<DeviceModel> list(@Param("cid") Long categoryId, @Param("bid") Long brandId);
}

