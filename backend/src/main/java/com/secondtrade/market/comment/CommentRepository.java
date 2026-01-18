package com.secondtrade.market.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<ProductComment, Long> {
  List<ProductComment> findByProductIdOrderByIdAsc(Long productId);
}

