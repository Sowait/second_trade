package com.secondtrade.market.draft;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftRepository extends JpaRepository<Draft, Long> {
  Optional<Draft> findByDraftKey(String draftKey);
}

