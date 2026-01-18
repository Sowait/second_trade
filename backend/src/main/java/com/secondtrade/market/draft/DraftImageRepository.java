package com.secondtrade.market.draft;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftImageRepository extends JpaRepository<DraftImage, Long> {
  List<DraftImage> findByDraftIdOrderBySortOrderAsc(Long draftId);
}

