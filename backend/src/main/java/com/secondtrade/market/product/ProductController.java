package com.secondtrade.market.product;

import com.secondtrade.common.ApiException;
import com.secondtrade.market.comment.CommentRepository;
import com.secondtrade.market.comment.ProductComment;
import com.secondtrade.security.SecurityUtil;
import com.secondtrade.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market/products")
/**
 * 商品与留言接口。
 *
 * <p>市场大厅：不传 seller_id，返回上架中商品</p>
 * <p>卖家中心：传 seller_id，需要登录且 seller_id 必须为自己（管理员可查看所有）</p>
 */
public class ProductController {
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;
  private final CommentRepository commentRepository;

  public ProductController(
    ProductRepository productRepository,
    ProductImageRepository productImageRepository,
    CommentRepository commentRepository
  ) {
    this.productRepository = productRepository;
    this.productImageRepository = productImageRepository;
    this.commentRepository = commentRepository;
  }

  /**
   * 商品列表。
   * <p>GET /api/market/products/?page=&page_size=&category_id=</p>
   * <p>卖家中心：GET /api/market/products/?seller_id=</p>
   */
  @GetMapping({"/", ""})
  @Transactional(readOnly = true)
  public Map<String, Object> list(
    @RequestParam(value = "seller_id", required = false) Long sellerId,
    @RequestParam(value = "category_id", required = false) Long categoryId,
    @RequestParam(value = "brand", required = false) String brand,
    @RequestParam(value = "condition", required = false) String condition,
    @RequestParam(value = "priceRange", required = false) String priceRange,
    @RequestParam(value = "sort", required = false) String sort,
    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "limit", required = false) Integer limit
  ) {
    int ps = 24;
    if (pageSize != null) ps = pageSize;
    if (limit != null) ps = limit;
    ps = Math.min(100, Math.max(1, ps));
    int p = Math.max(1, page) - 1;

    Sort sortObj = toSort(sort);
    PageRequest pageable = PageRequest.of(p, ps, sortObj);

    Page<Product> result;
    if (sellerId != null) {
      User me = SecurityUtil.requireUser();
      boolean isAdmin = "admin".equalsIgnoreCase(me.getRole()) || me.isSuperuser() || me.isStaff();
      if (!isAdmin && !me.getId().equals(sellerId)) throw new ApiException(403, "forbidden");
      Specification<Product> spec = buildSpec(sellerId, categoryId, brand, condition, priceRange, null);
      result = productRepository.findAll(spec, pageable);
    } else {
      Specification<Product> spec = buildSpec(null, categoryId, brand, condition, priceRange, ProductStatus.on_sale);
      result = productRepository.findAll(spec, pageable);
    }

    List<Map<String, Object>> items = result.getContent().stream().map(this::toListItem).collect(Collectors.toList());
    Map<String, Object> res = new HashMap<>();
    res.put("count", result.getTotalElements());
    res.put("results", items);
    return res;
  }

  private Sort toSort(String sort) {
    String s = sort == null ? "" : sort.trim().toLowerCase(Locale.ROOT);
    if (s.isEmpty() || "recommend".equals(s)) {
      return Sort.by(Sort.Direction.DESC, "favoriteCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    if ("price-asc".equals(s)) {
      return Sort.by(Sort.Direction.ASC, "sellingPrice").and(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    if ("price-desc".equals(s)) {
      return Sort.by(Sort.Direction.DESC, "sellingPrice").and(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    if ("newest".equals(s)) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }
    if ("hottest".equals(s)) {
      return Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "favoriteCount"));
    }
    return Sort.by(Sort.Direction.DESC, "createdAt");
  }

  private Specification<Product> buildSpec(
    Long sellerId,
    Long categoryId,
    String brand,
    String condition,
    String priceRange,
    ProductStatus status
  ) {
    return (root, query, cb) -> {
      query.distinct(true);
      List<Predicate> predicates = new ArrayList<>();

      if (sellerId != null) {
        predicates.add(cb.equal(root.get("seller").get("id"), sellerId));
      }
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (categoryId != null) {
        predicates.add(cb.equal(root.get("category").get("id"), categoryId));
      }

      String brandNorm = normalizeToken(brand);
      if (!brandNorm.isEmpty()) {
        Join<?, ?> deviceModelJoin = root.join("deviceModel", JoinType.LEFT);
        Join<?, ?> brandJoin = deviceModelJoin.join("brand", JoinType.LEFT);
        predicates.add(cb.like(cb.lower(brandJoin.get("name")), "%" + brandNorm + "%"));
      }

      Set<String> gradeLabels = toGradeLabels(condition);
      if (!gradeLabels.isEmpty()) {
        predicates.add(root.get("gradeLabel").in(gradeLabels));
      }

      PriceRange pr = parsePriceRange(priceRange);
      if (pr.min != null) predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), pr.min));
      if (pr.max != null) predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), pr.max));

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private String normalizeToken(String s) {
    if (s == null) return "";
    String v = s.trim();
    if (v.isEmpty()) return "";
    return v.toLowerCase(Locale.ROOT);
  }

  private Set<String> toGradeLabels(String condition) {
    String c = normalizeToken(condition);
    if (c.isEmpty()) return java.util.Collections.emptySet();

    if ("a".equals(c) || "b".equals(c) || "c".equals(c)) return new HashSet<>(Arrays.asList(c.toUpperCase(Locale.ROOT)));

    if ("new".equals(c) || "like-new".equals(c) || "excellent".equals(c)) {
      return new HashSet<>(Arrays.asList("A"));
    }
    if ("good".equals(c)) {
      return new HashSet<>(Arrays.asList("B"));
    }
    if ("fair".equals(c)) {
      return new HashSet<>(Arrays.asList("C"));
    }

    return java.util.Collections.emptySet();
  }

  private static class PriceRange {
    Integer min;
    Integer max;
  }

  private PriceRange parsePriceRange(String priceRange) {
    PriceRange pr = new PriceRange();
    String s = normalizeToken(priceRange);
    if (s.isEmpty()) return pr;

    try {
      if (s.endsWith("+")) {
        String left = s.substring(0, s.length() - 1).trim();
        if (!left.isEmpty()) pr.min = Integer.parseInt(left);
        return pr;
      }
      int idx = s.indexOf("-");
      if (idx > 0) {
        String left = s.substring(0, idx).trim();
        String right = s.substring(idx + 1).trim();
        if (!left.isEmpty()) pr.min = Integer.parseInt(left);
        if (!right.isEmpty()) pr.max = Integer.parseInt(right);
      }
    } catch (NumberFormatException ignored) {
      return new PriceRange();
    }

    return pr;
  }

  /**
   * 商品详情（会累加 view_count）。
   * <p>GET /api/market/products/{id}/</p>
   */
  @GetMapping({"/{id}/", "/{id}"})
  @Transactional
  public Map<String, Object> detail(@PathVariable("id") Long id) {
    Product p = productRepository.findById(id).orElse(null);
    if (p == null) return null;
    p.setViewCount((p.getViewCount() == null ? 0 : p.getViewCount()) + 1);
    productRepository.save(p);

    Map<String, Object> res = toListItem(p);
    List<ProductImage> imgs = productImageRepository.findByProductIdOrderBySortOrderAsc(p.getId());
    List<Object> images = imgs.stream().map(i -> i.getUrl()).collect(Collectors.toList());
    res.put("images", images);

    Map<String, Object> seller = new HashMap<>();
    if (p.getSeller() != null) {
      seller.put("id", p.getSeller().getId());
      seller.put("username", p.getSeller().getUsername());
      seller.put("nickname", p.getSeller().getNickname());
    }
    res.put("seller", seller);
    res.put("seller_id", p.getSeller() != null ? p.getSeller().getId() : null);
    res.put("seller_name", p.getSeller() != null ? (p.getSeller().getNickname() != null ? p.getSeller().getNickname() : p.getSeller().getUsername()) : null);
    res.put("seller_address", p.getSeller() != null ? p.getSeller().getAddress() : null);

    return res;
  }

  /**
   * 获取留言列表。
   * <p>GET /api/market/products/{productId}/comments/</p>
   */
  @GetMapping({"/{productId}/comments/", "/{productId}/comments"})
  @Transactional(readOnly = true)
  public Map<String, Object> comments(@PathVariable("productId") Long productId) {
    List<ProductComment> list = commentRepository.findByProductIdOrderByIdAsc(productId);
    List<Map<String, Object>> results = list.stream().map(c -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", c.getId());
      m.put("product_id", productId);
      m.put("sender_id", c.getSender() != null ? c.getSender().getId() : null);
      m.put("sender_name", c.getSender() != null ? (c.getSender().getNickname() != null ? c.getSender().getNickname() : c.getSender().getUsername()) : "用户");
      m.put("sender_role", c.getSenderRole());
      m.put("content", c.getContent());
      m.put("created_at", c.getCreatedAt());
      return m;
    }).collect(Collectors.toList());
    Map<String, Object> res = new HashMap<>();
    res.put("count", results.size());
    res.put("results", results);
    return res;
  }

  public static class CommentReq {
    @NotBlank
    private String content;

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }

  /**
   * 发送留言。
   * <p>POST /api/market/products/{productId}/comments/</p>
   */
  @PostMapping({"/{productId}/comments/", "/{productId}/comments"})
  @Transactional
  public Map<String, Object> addComment(@PathVariable("productId") Long productId, @RequestBody CommentReq req) {
    User me = SecurityUtil.requireUser();
    Product p = productRepository.findById(productId).orElse(null);
    if (p == null) throw new ApiException(404, "商品不存在");
    if (req == null || req.getContent() == null || req.getContent().trim().isEmpty()) throw new ApiException(400, "content 不能为空");

    ProductComment c = new ProductComment();
    c.setProduct(p);
    c.setSender(me);
    boolean isSeller = p.getSeller() != null && p.getSeller().getId() != null && p.getSeller().getId().equals(me.getId());
    c.setSenderRole(isSeller ? "seller" : "buyer");
    c.setContent(req.getContent().trim());
    c.setCreatedAt(LocalDateTime.now());
    commentRepository.save(c);

    Map<String, Object> res = new HashMap<>();
    res.put("id", c.getId());
    res.put("product_id", productId);
    res.put("sender_id", me.getId());
    res.put("sender_name", me.getNickname() != null ? me.getNickname() : me.getUsername());
    res.put("sender_role", c.getSenderRole());
    res.put("content", c.getContent());
    res.put("created_at", c.getCreatedAt());
    return res;
  }

  private Map<String, Object> toListItem(Product p) {
    Map<String, Object> m = new HashMap<>();
    m.put("id", p.getId());
    m.put("seller_id", p.getSeller() != null ? p.getSeller().getId() : null);
    m.put("category_id", p.getCategory() != null ? p.getCategory().getId() : null);
    m.put("category_name", p.getCategory() != null ? p.getCategory().getName() : null);
    m.put("device_model_id", p.getDeviceModel() != null ? p.getDeviceModel().getId() : null);
    m.put("device_model_name", p.getDeviceModel() != null ? p.getDeviceModel().getName() : null);
    m.put("brand_text", (p.getDeviceModel() != null && p.getDeviceModel().getBrand() != null) ? p.getDeviceModel().getBrand().getName() : null);
    m.put("title", p.getTitle());
    m.put("school", p.getSchool());
    m.put("product_summary", p.getProductSummary());
    m.put("description", p.getDescription());
    m.put("grade_label", p.getGradeLabel());
    m.put("years_used", p.getYearsUsed());
    m.put("defects", p.getDefects());
    m.put("main_image", p.getMainImage());
    m.put("main_image_url", p.getMainImage());
    m.put("selling_price", p.getSellingPrice());
    m.put("original_price", p.getOriginalPrice());
    m.put("favorite_count", p.getFavoriteCount());
    m.put("view_count", p.getViewCount());
    m.put("market_tag", p.getMarketTag());
    m.put("created_at", p.getCreatedAt());
    m.put("status", p.getStatus() != null ? p.getStatus().name() : null);
    m.put("review_reject_reason", p.getReviewRejectReason());
    return m;
  }
}
