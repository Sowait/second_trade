package com.secondtrade.market.draft;

import com.secondtrade.common.ApiException;
import com.secondtrade.market.category.Category;
import com.secondtrade.market.category.CategoryRepository;
import com.secondtrade.market.draft.dto.DraftInitReq;
import com.secondtrade.market.draft.dto.EstimateReq;
import com.secondtrade.market.draft.dto.PublishReq;
import com.secondtrade.market.device.DeviceModel;
import com.secondtrade.market.device.DeviceModelRepository;
import com.secondtrade.market.product.Product;
import com.secondtrade.market.product.ProductImage;
import com.secondtrade.market.product.ProductImageRepository;
import com.secondtrade.market.product.ProductRepository;
import com.secondtrade.market.product.ProductStatus;
import com.secondtrade.market.storage.FileStorageService;
import com.secondtrade.market.util.Keys;
import com.secondtrade.security.SecurityUtil;
import com.secondtrade.user.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/market/drafts")
/**
 * 商品上架草稿接口（需要登录）。
 *
 * <p>流程：init 创建草稿 -> images 上传图片 -> estimate 估价 -> publish 发布</p>
 */
public class DraftController {
  private final DraftRepository draftRepository;
  private final DraftImageRepository draftImageRepository;
  private final FileStorageService fileStorageService;
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;
  private final CategoryRepository categoryRepository;
  private final DeviceModelRepository deviceModelRepository;

  public DraftController(
    DraftRepository draftRepository,
    DraftImageRepository draftImageRepository,
    FileStorageService fileStorageService,
    ProductRepository productRepository,
    ProductImageRepository productImageRepository,
    CategoryRepository categoryRepository,
    DeviceModelRepository deviceModelRepository
  ) {
    this.draftRepository = draftRepository;
    this.draftImageRepository = draftImageRepository;
    this.fileStorageService = fileStorageService;
    this.productRepository = productRepository;
    this.productImageRepository = productImageRepository;
    this.categoryRepository = categoryRepository;
    this.deviceModelRepository = deviceModelRepository;
  }

  /**
   * 创建上架草稿。
   * <p>POST /api/market/drafts/init/</p>
   * <p>返回：draft_key + meta</p>
   */
  @PostMapping({"/init/", "/init"})
  public Map<String, Object> init(@Valid @RequestBody DraftInitReq req) {
    User me = SecurityUtil.requireUser();
    Draft d = new Draft();
    d.setDraftKey(Keys.draftKey());
    d.setSeller(me);
    d.setCategoryId(req.getCategory_id());
    d.setDeviceModelId(req.getDevice_model_id());
    d.setYearsUsed(req.getYears_used());
    d.setOriginalPrice(req.getOriginal_price());
    d.setCreatedAt(LocalDateTime.now());
    draftRepository.save(d);

    Map<String, Object> meta = new HashMap<>();
    meta.put("category_id", d.getCategoryId());
    meta.put("device_model_id", d.getDeviceModelId());
    meta.put("years_used", d.getYearsUsed());
    meta.put("original_price", d.getOriginalPrice());

    Map<String, Object> res = new HashMap<>();
    res.put("draft_key", d.getDraftKey());
    res.put("meta", meta);
    return res;
  }

  @PostMapping({"/init_from_product/{productId}/", "/init_from_product/{productId}"})
  @Transactional
  public Map<String, Object> initFromProduct(@PathVariable("productId") Long productId) {
    User me = SecurityUtil.requireUser();
    Product p = productRepository.findById(productId).orElse(null);
    if (p == null) throw new ApiException(404, "商品不存在");
    if (p.getSeller() == null || p.getSeller().getId() == null || !p.getSeller().getId().equals(me.getId())) {
      throw new ApiException(403, "forbidden");
    }
    if (p.getStatus() != ProductStatus.rejected) throw new ApiException(400, "仅支持编辑审核不通过的商品");

    Draft d = new Draft();
    d.setDraftKey(Keys.draftKey());
    d.setSeller(me);
    d.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
    d.setDeviceModelId(p.getDeviceModel() != null ? p.getDeviceModel().getId() : null);
    d.setYearsUsed(p.getYearsUsed());
    d.setOriginalPrice(p.getOriginalPrice());
    d.setCreatedAt(LocalDateTime.now());
    draftRepository.save(d);

    List<ProductImage> productImages = productImageRepository.findByProductIdOrderBySortOrderAsc(p.getId());
    if (productImages != null && !productImages.isEmpty()) {
      List<DraftImage> draftImages = productImages.stream().map(pi -> {
        DraftImage di = new DraftImage();
        di.setDraft(d);
        di.setUrl(pi.getUrl());
        di.setSortOrder(pi.getSortOrder());
        di.setMainImage(pi.isMainImage());
        return di;
      }).collect(Collectors.toList());
      draftImageRepository.saveAll(draftImages);
    }

    Map<String, Object> meta = new HashMap<>();
    meta.put("category_id", d.getCategoryId());
    meta.put("device_model_id", d.getDeviceModelId());
    meta.put("years_used", d.getYearsUsed());
    meta.put("original_price", d.getOriginalPrice());

    Map<String, Object> product = new HashMap<>();
    product.put("id", p.getId());
    product.put("title", p.getTitle());
    product.put("school", p.getSchool());
    product.put("product_summary", p.getProductSummary());
    product.put("description", p.getDescription());
    product.put("selling_price", p.getSellingPrice());
    product.put("status", p.getStatus() != null ? p.getStatus().name() : null);
    product.put("review_reject_reason", p.getReviewRejectReason());

    Map<String, Object> res = new HashMap<>();
    res.put("draft_key", d.getDraftKey());
    res.put("meta", meta);
    res.put("product", product);
    if (productImages != null) {
      res.put("images", productImages.stream().map(ProductImage::getUrl).collect(Collectors.toList()));
    } else {
      res.put("images", java.util.Collections.emptyList());
    }
    return res;
  }

  /**
   * 上传草稿图片（最多 4 张，第一张为主图）。
   * <p>POST /api/market/drafts/{draftKey}/images/</p>
   * <p>Content-Type: multipart/form-data，字段名必须为 image</p>
   */
  @PostMapping({"/{draftKey}/images/", "/{draftKey}/images"})
  public Map<String, Object> uploadImage(@PathVariable("draftKey") String draftKey, @RequestParam("image") MultipartFile image) {
    User me = SecurityUtil.requireUser();
    Draft d = draftRepository.findByDraftKey(draftKey).orElse(null);
    if (d == null) throw new ApiException(404, "draft 不存在");
    if (d.getSeller() == null || !d.getSeller().getId().equals(me.getId())) throw new ApiException(403, "forbidden");

    List<DraftImage> existing = draftImageRepository.findByDraftIdOrderBySortOrderAsc(d.getId());
    int sortOrder = existing.size();
    if (sortOrder >= 4) throw new ApiException(400, "最多4张图片");

    String url = fileStorageService.saveDraftImage(draftKey, image, sortOrder);
    DraftImage di = new DraftImage();
    di.setDraft(d);
    di.setUrl(url);
    di.setSortOrder(sortOrder);
    di.setMainImage(sortOrder == 0);
    draftImageRepository.save(di);
    Map<String, Object> res = new HashMap<>();
    res.put("id", di.getId());
    res.put("image", url);
    return res;
  }

  /**
   * 草稿估价（当前为简单折旧算法示例）。
   * <p>POST /api/market/drafts/{draftKey}/estimate/</p>
   * <p>返回：estimated_min / estimated_max / estimated_mid</p>
   */
  @PostMapping({"/{draftKey}/estimate/", "/{draftKey}/estimate"})
  public Map<String, Object> estimate(@PathVariable("draftKey") String draftKey, @Valid @RequestBody EstimateReq req) {
    SecurityUtil.requireUser();
    int original = req.getOriginal_price() == null ? 0 : req.getOriginal_price();
    int years = req.getYears_used() == null ? 0 : req.getYears_used();
    double factor = 0.85;
    factor -= Math.min(0.5, years * 0.08);
    int mid = (int) Math.max(1, Math.round(original * factor));
    int min = (int) Math.max(1, Math.round(mid * 0.92));
    int max = (int) Math.max(1, Math.round(mid * 1.08));
    Map<String, Object> res = new HashMap<>();
    res.put("estimated_min", min);
    res.put("estimated_max", max);
    res.put("estimated_mid", mid);
    return res;
  }

  /**
   * 草稿识别（占位接口，用于兼容前端封装；当前返回固定示例数据）。
   * <p>POST /api/market/drafts/{draftKey}/analyze/</p>
   */
  @PostMapping({"/{draftKey}/analyze/", "/{draftKey}/analyze"})
  public Map<String, Object> analyze(@PathVariable("draftKey") String draftKey) {
    User me = SecurityUtil.requireUser();
    Draft d = draftRepository.findByDraftKey(draftKey).orElse(null);
    if (d == null) throw new ApiException(404, "draft 不存在");
    if (d.getSeller() == null || !d.getSeller().getId().equals(me.getId())) throw new ApiException(403, "forbidden");
    List<DraftImage> imgs = draftImageRepository.findByDraftIdOrderBySortOrderAsc(d.getId());
    String main = imgs.isEmpty() ? null : imgs.get(0).getUrl();
    Map<String, Object> res = new HashMap<>();
    res.put("main_image", main);
    res.put("grade_label", "A");
    res.put("grade_score", 95);
    res.put("defects", java.util.Arrays.asList("scratch_screen", "dent_body"));
    return res;
  }

  /**
   * 发布草稿为商品（进入待审核状态）。
   * <p>POST /api/market/drafts/{draftKey}/publish/</p>
   */
  @PostMapping({"/{draftKey}/publish/", "/{draftKey}/publish"})
  public Map<String, Object> publish(@PathVariable("draftKey") String draftKey, @Valid @RequestBody PublishReq req) {
    User me = SecurityUtil.requireUser();
    Draft d = draftRepository.findByDraftKey(draftKey).orElse(null);
    if (d == null) throw new ApiException(404, "draft 不存在");
    if (d.getSeller() == null || !d.getSeller().getId().equals(me.getId())) throw new ApiException(403, "forbidden");

    List<DraftImage> imgs = draftImageRepository.findByDraftIdOrderBySortOrderAsc(d.getId());
    if (imgs.isEmpty()) throw new ApiException(400, "请先上传图片");

    Category category = categoryRepository.findById(req.getCategory_id()).orElse(null);
    DeviceModel deviceModel = deviceModelRepository.findById(req.getDevice_model_id()).orElse(null);
    if (category == null) throw new ApiException(400, "category_id 不合法");
    if (deviceModel == null) throw new ApiException(400, "device_model_id 不合法");

    Product p = new Product();
    p.setSeller(me);
    p.setCategory(category);
    p.setDeviceModel(deviceModel);
    p.setTitle(req.getTitle());
    p.setSchool(req.getSchool());
    p.setProductSummary(req.getProduct_summary());
    p.setDescription(req.getDescription());
    p.setSellingPrice(req.getSelling_price());
    p.setOriginalPrice(req.getOriginal_price());
    p.setYearsUsed(req.getYears_used());
    p.setGradeLabel("A");
    p.setDefects(java.util.Collections.emptyList());
    p.setMainImage(imgs.get(0).getUrl());
    p.setStatus(ProductStatus.pending_review);
    p.setFavoriteCount(0);
    p.setViewCount(0);
    p.setMarketTag("New");
    p.setCreatedAt(LocalDateTime.now());
    productRepository.save(p);

    List<ProductImage> pis = imgs.stream().map(di -> {
      ProductImage pi = new ProductImage();
      pi.setProduct(p);
      pi.setUrl(di.getUrl());
      pi.setSortOrder(di.getSortOrder());
      pi.setMainImage(di.isMainImage());
      return pi;
    }).collect(Collectors.toList());
    productImageRepository.saveAll(pis);

    Map<String, Object> res = new HashMap<>();
    res.put("product_id", p.getId());
    res.put("status", "published");
    return res;
  }

  @PostMapping({"/{draftKey}/publish_update/{productId}/", "/{draftKey}/publish_update/{productId}"})
  @Transactional
  public Map<String, Object> publishUpdate(
    @PathVariable("draftKey") String draftKey,
    @PathVariable("productId") Long productId,
    @Valid @RequestBody PublishReq req
  ) {
    User me = SecurityUtil.requireUser();
    Draft d = draftRepository.findByDraftKey(draftKey).orElse(null);
    if (d == null) throw new ApiException(404, "draft 不存在");
    if (d.getSeller() == null || !d.getSeller().getId().equals(me.getId())) throw new ApiException(403, "forbidden");

    Product p = productRepository.findById(productId).orElse(null);
    if (p == null) throw new ApiException(404, "商品不存在");
    if (p.getSeller() == null || p.getSeller().getId() == null || !p.getSeller().getId().equals(me.getId())) {
      throw new ApiException(403, "forbidden");
    }
    if (p.getStatus() != ProductStatus.rejected) throw new ApiException(400, "仅支持编辑审核不通过的商品");

    List<DraftImage> imgs = draftImageRepository.findByDraftIdOrderBySortOrderAsc(d.getId());
    if (imgs.isEmpty()) throw new ApiException(400, "请先上传图片");

    Category category = categoryRepository.findById(req.getCategory_id()).orElse(null);
    DeviceModel deviceModel = deviceModelRepository.findById(req.getDevice_model_id()).orElse(null);
    if (category == null) throw new ApiException(400, "category_id 不合法");
    if (deviceModel == null) throw new ApiException(400, "device_model_id 不合法");

    p.setCategory(category);
    p.setDeviceModel(deviceModel);
    p.setTitle(req.getTitle());
    p.setSchool(req.getSchool());
    p.setProductSummary(req.getProduct_summary());
    p.setDescription(req.getDescription());
    p.setSellingPrice(req.getSelling_price());
    p.setOriginalPrice(req.getOriginal_price());
    p.setYearsUsed(req.getYears_used());

    DraftImage main = imgs.stream().filter(DraftImage::isMainImage).findFirst().orElse(imgs.get(0));
    p.setMainImage(main.getUrl());
    p.setStatus(ProductStatus.pending_review);
    p.setReviewRejectReason(null);
    productRepository.save(p);

    List<ProductImage> old = productImageRepository.findByProductIdOrderBySortOrderAsc(p.getId());
    if (old != null && !old.isEmpty()) productImageRepository.deleteAll(old);

    List<ProductImage> pis = imgs.stream().map(di -> {
      ProductImage pi = new ProductImage();
      pi.setProduct(p);
      pi.setUrl(di.getUrl());
      pi.setSortOrder(di.getSortOrder());
      pi.setMainImage(di.isMainImage());
      return pi;
    }).collect(Collectors.toList());
    productImageRepository.saveAll(pis);

    Map<String, Object> res = new HashMap<>();
    res.put("product_id", p.getId());
    res.put("status", p.getStatus() != null ? p.getStatus().name() : null);
    return res;
  }
}
