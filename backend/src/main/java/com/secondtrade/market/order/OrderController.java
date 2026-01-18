package com.secondtrade.market.order;

import com.secondtrade.common.ApiException;
import com.secondtrade.market.product.Product;
import com.secondtrade.market.product.ProductImage;
import com.secondtrade.market.product.ProductImageRepository;
import com.secondtrade.market.product.ProductRepository;
import com.secondtrade.market.product.ProductStatus;
import com.secondtrade.market.util.Keys;
import com.secondtrade.security.SecurityUtil;
import com.secondtrade.user.User;
import com.secondtrade.user.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market/orders")
/**
 * 订单接口（需要登录）。
 *
 * <p>订单状态参考前端 OrderCenterView：pending_payment / pending_shipment / shipped / completed / refunded</p>
 */
public class OrderController {
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;
  private final UserRepository userRepository;

  public OrderController(
    OrderRepository orderRepository,
    ProductRepository productRepository,
    ProductImageRepository productImageRepository,
    UserRepository userRepository
  ) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.productImageRepository = productImageRepository;
    this.userRepository = userRepository;
  }

  public static class CreateTradeReq {
    private Long product_id;

    public Long getProduct_id() {
      return product_id;
    }

    public void setProduct_id(Long product_id) {
      this.product_id = product_id;
    }
  }

  /**
   * 创建订单（点击“立即购买”）。
   * <p>POST /api/market/orders/create_trade/</p>
   */
  @PostMapping({"/create_trade/", "/create_trade"})
  @Transactional
  public Map<String, Object> createTrade(@RequestBody CreateTradeReq req) {
    User me = SecurityUtil.requireUser();
    if (req == null || req.getProduct_id() == null) throw new ApiException(400, "product_id 缺失");
    Product p = productRepository.findById(req.getProduct_id()).orElse(null);
    if (p == null) throw new ApiException(404, "商品不存在");
    if (p.getStatus() != ProductStatus.on_sale) throw new ApiException(400, "商品当前不可购买");
    if (p.getSeller() != null && p.getSeller().getId().equals(me.getId())) throw new ApiException(400, "不能购买自己的商品");

    TradeOrder o = new TradeOrder();
    o.setBuyer(me);
    o.setSeller(p.getSeller());
    o.setProduct(p);
    o.setStatus(OrderStatus.pending_payment);
    o.setPickup(false);
    o.setAmount(p.getSellingPrice() == null ? 0 : p.getSellingPrice());
    o.setCreatedAt(LocalDateTime.now());
    orderRepository.save(o);
    o.setOrderNo(Keys.orderNo(o.getId()));
    orderRepository.save(o);

    Map<String, Object> res = new HashMap<>();
    res.put("order_id", o.getId());
    res.put("order_no", o.getOrderNo());
    res.put("status", o.getStatus().name());
    res.put("product_id", p.getId());
    return res;
  }

  public static class PayReq {
    private Boolean pickup;

    public Boolean getPickup() {
      return pickup;
    }

    public void setPickup(Boolean pickup) {
      this.pickup = pickup;
    }
  }

  /**
   * 支付订单（可选自提）。
   * <p>POST /api/market/orders/{orderId}/pay/</p>
   */
  @PostMapping("/{orderId}/pay/")
  @Transactional
  public Map<String, Object> pay(@PathVariable("orderId") Long orderId, @RequestBody(required = false) PayReq req) {
    User me = SecurityUtil.requireUser();
    TradeOrder o = orderRepository.findById(orderId).orElse(null);
    if (o == null) throw new ApiException(404, "订单不存在");
    if (o.getBuyer() == null || !o.getBuyer().getId().equals(me.getId())) throw new ApiException(403, "forbidden");
    if (o.getStatus() != OrderStatus.pending_payment) throw new ApiException(400, "订单状态不可支付");
    boolean pickup = req != null && Boolean.TRUE.equals(req.getPickup());
    o.setPickup(pickup);
    o.setStatus(OrderStatus.pending_shipment);
    o.setPaidAt(LocalDateTime.now());
    orderRepository.save(o);
    return actionResp(o);
  }

  /**
   * 取消支付/取消订单（买家）。
   * <p>POST /api/market/orders/{orderId}/cancel_payment/</p>
   */
  @PostMapping("/{orderId}/cancel_payment/")
  @Transactional
  public Map<String, Object> cancelPayment(@PathVariable("orderId") Long orderId) {
    User me = SecurityUtil.requireUser();
    TradeOrder o = orderRepository.findById(orderId).orElse(null);
    if (o == null) throw new ApiException(404, "订单不存在");
    if (o.getBuyer() == null || !o.getBuyer().getId().equals(me.getId())) throw new ApiException(403, "forbidden");
    if (o.getStatus() == OrderStatus.completed) throw new ApiException(400, "订单已完成");
    o.setStatus(OrderStatus.refunded);
    orderRepository.save(o);
    return actionResp(o);
  }

  /**
   * 卖家发货（非自提订单）。
   * <p>POST /api/market/orders/{orderId}/ship/</p>
   */
  @PostMapping("/{orderId}/ship/")
  @Transactional
  public Map<String, Object> ship(@PathVariable("orderId") Long orderId) {
    User me = SecurityUtil.requireUser();
    TradeOrder o = orderRepository.findById(orderId).orElse(null);
    if (o == null) throw new ApiException(404, "订单不存在");
    if (o.getSeller() == null || !o.getSeller().getId().equals(me.getId())) throw new ApiException(403, "forbidden");
    if (o.isPickup()) throw new ApiException(400, "自提订单无需发货");
    if (o.getStatus() != OrderStatus.pending_shipment) throw new ApiException(400, "订单状态不可发货");
    o.setStatus(OrderStatus.shipped);
    orderRepository.save(o);
    return actionResp(o);
  }

  /**
   * 买家确认收货/确认自提。
   * <p>POST /api/market/orders/{orderId}/confirm_receipt/</p>
   */
  @PostMapping("/{orderId}/confirm_receipt/")
  @Transactional
  public Map<String, Object> confirmReceipt(@PathVariable("orderId") Long orderId) {
    User me = SecurityUtil.requireUser();
    TradeOrder o = orderRepository.findById(orderId).orElse(null);
    if (o == null) throw new ApiException(404, "订单不存在");
    if (o.getBuyer() == null || !o.getBuyer().getId().equals(me.getId())) throw new ApiException(403, "forbidden");
    if (o.isPickup()) {
      if (o.getStatus() != OrderStatus.pending_shipment) throw new ApiException(400, "订单状态不可确认自提");
      o.setStatus(OrderStatus.completed);
    } else {
      if (o.getStatus() != OrderStatus.shipped && o.getStatus() != OrderStatus.pending_receipt) throw new ApiException(400, "订单状态不可确认收货");
      o.setStatus(OrderStatus.completed);
    }
    orderRepository.save(o);
    return actionResp(o);
  }

  /**
   * 买家申请退款/取消订单。
   * <p>POST /api/market/orders/{orderId}/refund/</p>
   */
  @PostMapping("/{orderId}/refund/")
  @Transactional
  public Map<String, Object> refund(@PathVariable("orderId") Long orderId) {
    User me = SecurityUtil.requireUser();
    TradeOrder o = orderRepository.findById(orderId).orElse(null);
    if (o == null) throw new ApiException(404, "订单不存在");
    if (o.getBuyer() == null || !o.getBuyer().getId().equals(me.getId())) throw new ApiException(403, "forbidden");
    if (o.getStatus() == OrderStatus.completed) throw new ApiException(400, "订单已完成");
    o.setStatus(OrderStatus.refunded);
    orderRepository.save(o);
    return actionResp(o);
  }

  /**
   * 买家订单列表。
   * <p>GET /api/market/orders/buy/</p>
   */
  @GetMapping({"/buy/", "/buy"})
  @Transactional(readOnly = true)
  public List<Map<String, Object>> buy() {
    User me = SecurityUtil.requireUser();
    Page<TradeOrder> page = orderRepository.findByBuyerIdOrderByIdDesc(me.getId(), PageRequest.of(0, 200));
    return page.getContent().stream().map(this::toOrderListItem).collect(Collectors.toList());
  }

  /**
   * 卖家订单列表。
   * <p>GET /api/market/orders/sell/</p>
   */
  @GetMapping({"/sell/", "/sell"})
  @Transactional(readOnly = true)
  public List<Map<String, Object>> sell() {
    User me = SecurityUtil.requireUser();
    Page<TradeOrder> page = orderRepository.findBySellerIdOrderByIdDesc(me.getId(), PageRequest.of(0, 200));
    return page.getContent().stream().map(this::toOrderListItem).collect(Collectors.toList());
  }

  /**
   * 订单详情（买家/卖家/管理员可看）。
   * <p>GET /api/market/orders/{orderId}/</p>
   */
  @GetMapping("/{orderId}/")
  @Transactional(readOnly = true)
  public Map<String, Object> detail(@PathVariable("orderId") Long orderId) {
    User me = SecurityUtil.requireUser();
    TradeOrder o = orderRepository.findById(orderId).orElse(null);
    if (o == null) return null;
    boolean isBuyer = o.getBuyer() != null && o.getBuyer().getId().equals(me.getId());
    boolean isSeller = o.getSeller() != null && o.getSeller().getId().equals(me.getId());
    boolean isAdmin = "admin".equalsIgnoreCase(me.getRole()) || me.isSuperuser() || me.isStaff();
    if (!isBuyer && !isSeller && !isAdmin) throw new ApiException(403, "forbidden");

    Map<String, Object> base = toOrderListItem(o);
    Product p = o.getProduct();
    if (p != null) {
      List<ProductImage> imgs = productImageRepository.findByProductIdOrderBySortOrderAsc(p.getId());
      List<String> urls = imgs.stream().map(ProductImage::getUrl).collect(Collectors.toList());
      Map<String, Object> product = new HashMap<>();
      product.put("id", p.getId());
      product.put("title", p.getTitle());
      product.put("selling_price", p.getSellingPrice());
      product.put("images", urls);
      base.put("product", product);
    }
    base.put("buyer_address", o.getBuyer() != null ? o.getBuyer().getAddress() : null);
    base.put("seller_address", o.getSeller() != null ? o.getSeller().getAddress() : null);
    return base;
  }

  private Map<String, Object> actionResp(TradeOrder o) {
    Map<String, Object> res = new HashMap<>();
    res.put("order_id", o.getId());
    res.put("status", o.getStatus().name());
    return res;
  }

  private Map<String, Object> toOrderListItem(TradeOrder o) {
    Product p = o.getProduct();
    Map<String, Object> m = new HashMap<>();
    m.put("id", o.getId());
    m.put("order_no", o.getOrderNo());
    m.put("status", o.getStatus().name());
    m.put("buyer_id", o.getBuyer() != null ? o.getBuyer().getId() : null);
    m.put("seller_id", o.getSeller() != null ? o.getSeller().getId() : null);
    m.put("product_id", p != null ? p.getId() : null);
    m.put("product_title", p != null ? p.getTitle() : null);
    m.put("product_main_image", p != null ? p.getMainImage() : null);
    m.put("product_selling_price", o.getAmount());
    m.put("created_at", o.getCreatedAt());
    m.put("paid_at", o.getPaidAt());
    m.put("pickup", o.isPickup());
    if (o.isPickup() && o.getStatus() == OrderStatus.pending_shipment) {
      m.put("status_view", "待自提");
    }
    m.put("buyer_name", o.getBuyer() != null ? label(o.getBuyer()) : null);
    m.put("seller_name", o.getSeller() != null ? label(o.getSeller()) : null);
    return m;
  }

  private String label(User u) {
    if (u == null) return null;
    if (u.getNickname() != null && !u.getNickname().trim().isEmpty()) return u.getNickname();
    return u.getUsername();
  }
}
