package com.secondtrade.market.admin;

import com.secondtrade.common.ApiException;
import com.secondtrade.market.order.OrderRepository;
import com.secondtrade.market.order.OrderStatus;
import com.secondtrade.market.order.TradeOrder;
import com.secondtrade.market.product.Product;
import com.secondtrade.market.product.ProductRepository;
import com.secondtrade.market.product.ProductStatus;
import com.secondtrade.security.SecurityUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market/admin")
/**
 * 管理端接口（需要管理员权限）。
 *
 * <p>包含：商品审核、订单查询/导出源数据。</p>
 */
public class AdminMarketController {
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;

  public AdminMarketController(ProductRepository productRepository, OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
  }

  /**
   * 待审核商品列表。
   * <p>GET /api/market/admin/products/pending_review/?page=&page_size=&q=</p>
   */
  @GetMapping({"/products/pending_review/", "/products/pending_review"})
  @Transactional(readOnly = true)
  public Map<String, Object> pendingProducts(
    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
    @RequestParam(value = "page_size", required = false, defaultValue = "10") int pageSize,
    @RequestParam(value = "q", required = false, defaultValue = "") String q
  ) {
    SecurityUtil.requireAdmin();
    int p = Math.max(1, page) - 1;
    int ps = Math.min(100, Math.max(1, pageSize));
    Page<Product> result = productRepository.listByStatusAndCategory(ProductStatus.pending_review, null, PageRequest.of(p, ps));
    List<Map<String, Object>> list = result.getContent().stream().filter(prod -> {
      if (q == null || q.trim().isEmpty()) return true;
      String s = ((prod.getTitle() == null ? "" : prod.getTitle()) + " " + (prod.getSchool() == null ? "" : prod.getSchool())).toLowerCase();
      return s.contains(q.trim().toLowerCase());
    }).map(prod -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", prod.getId());
      m.put("title", prod.getTitle());
      m.put("seller_id", prod.getSeller() != null ? prod.getSeller().getId() : null);
      m.put("school", prod.getSchool());
      m.put("created_at", prod.getCreatedAt());
      m.put("selling_price", prod.getSellingPrice());
      m.put("main_image", prod.getMainImage());
      m.put("main_image_url", prod.getMainImage());
      return m;
    }).collect(Collectors.toList());

    Map<String, Object> res = new HashMap<>();
    res.put("count", result.getTotalElements());
    res.put("results", list);
    return res;
  }

  /**
   * 审核通过商品（上架）。
   * <p>POST /api/market/admin/products/{id}/approve/</p>
   */
  @PostMapping("/products/{id}/approve/")
  @Transactional
  public Map<String, Object> approve(@PathVariable("id") Long id) {
    SecurityUtil.requireAdmin();
    Product p = productRepository.findById(id).orElse(null);
    if (p == null) throw new ApiException(404, "商品不存在");
    p.setStatus(ProductStatus.on_sale);
    p.setReviewRejectReason(null);
    productRepository.save(p);
    return java.util.Collections.singletonMap("ok", true);
  }

  public static class RejectReq {
    private String reason;

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }
  }

  /**
   * 审核驳回商品（可带原因）。
   * <p>POST /api/market/admin/products/{id}/reject/</p>
   */
  @PostMapping("/products/{id}/reject/")
  @Transactional
  public Map<String, Object> reject(@PathVariable("id") Long id, @RequestBody(required = false) RejectReq req) {
    SecurityUtil.requireAdmin();
    Product p = productRepository.findById(id).orElse(null);
    if (p == null) throw new ApiException(404, "商品不存在");
    p.setStatus(ProductStatus.rejected);
    p.setReviewRejectReason(req != null ? req.getReason() : null);
    productRepository.save(p);
    return java.util.Collections.singletonMap("ok", true);
  }

  /**
   * 管理端订单列表（分页/筛选）。
   * <p>GET /api/market/admin/orders/?page=&page_size=&q=&status=&start=&end=</p>
   * <p>start/end 格式：yyyy-MM-dd</p>
   */
  @GetMapping({"/orders/", "/orders"})
  @Transactional(readOnly = true)
  public Map<String, Object> orders(
    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
    @RequestParam(value = "page_size", required = false, defaultValue = "10") int pageSize,
    @RequestParam(value = "q", required = false, defaultValue = "") String q,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "start", required = false) String start,
    @RequestParam(value = "end", required = false) String end
  ) {
    SecurityUtil.requireAdmin();
    int p = Math.max(1, page) - 1;
    int ps = Math.min(200, Math.max(1, pageSize));

    OrderStatus st = null;
    if (status != null && !status.trim().isEmpty()) {
      try {
        st = OrderStatus.valueOf(status.trim());
      } catch (Exception ignored) {
        st = null;
      }
    }

    LocalDateTime startAt = parseDateStart(start);
    LocalDateTime endAt = parseDateEnd(end);

    Page<TradeOrder> result = orderRepository.adminQuery(st, q, startAt, endAt, PageRequest.of(p, ps));
    List<Map<String, Object>> list = result.getContent().stream().map(o -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", o.getId());
      m.put("order_no", o.getOrderNo());
      m.put("status", o.getStatus() != null ? o.getStatus().name() : null);
      m.put("buyer_id", o.getBuyer() != null ? o.getBuyer().getId() : null);
      m.put("seller_id", o.getSeller() != null ? o.getSeller().getId() : null);
      m.put("buyer_name", o.getBuyer() != null ? (o.getBuyer().getNickname() != null ? o.getBuyer().getNickname() : o.getBuyer().getUsername()) : null);
      m.put("seller_name", o.getSeller() != null ? (o.getSeller().getNickname() != null ? o.getSeller().getNickname() : o.getSeller().getUsername()) : null);
      m.put("product_id", o.getProduct() != null ? o.getProduct().getId() : null);
      m.put("product_title", o.getProduct() != null ? o.getProduct().getTitle() : null);
      m.put("product_main_image", o.getProduct() != null ? o.getProduct().getMainImage() : null);
      m.put("product_selling_price", o.getAmount());
      m.put("amount", o.getAmount());
      m.put("created_at", o.getCreatedAt());
      m.put("paid_at", o.getPaidAt());
      m.put("pickup", o.isPickup());
      if (o.isPickup() && o.getStatus() == OrderStatus.pending_shipment) m.put("status_view", "待自提");
      return m;
    }).collect(Collectors.toList());

    Map<String, Object> res = new HashMap<>();
    res.put("count", result.getTotalElements());
    res.put("results", list);
    return res;
  }

  private static LocalDateTime parseDateStart(String raw) {
    if (raw == null || raw.trim().isEmpty()) return null;
    try {
      LocalDate d = LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_DATE);
      return d.atStartOfDay();
    } catch (Exception e) {
      return null;
    }
  }

  private static LocalDateTime parseDateEnd(String raw) {
    if (raw == null || raw.trim().isEmpty()) return null;
    try {
      LocalDate d = LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_DATE);
      return d.plusDays(1).atStartOfDay().minusSeconds(1);
    } catch (Exception e) {
      return null;
    }
  }
}
