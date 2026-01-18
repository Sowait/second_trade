package com.secondtrade.market.meta;

import com.secondtrade.market.brand.Brand;
import com.secondtrade.market.brand.BrandRepository;
import com.secondtrade.market.category.Category;
import com.secondtrade.market.category.CategoryRepository;
import com.secondtrade.market.device.DeviceModel;
import com.secondtrade.market.device.DeviceModelRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
/**
 * 市场基础数据接口（类目/品牌/型号）。
 *
 * <p>这些接口用于下拉选项与详情页名称展示。</p>
 */
public class MetaController {
  private final CategoryRepository categoryRepository;
  private final BrandRepository brandRepository;
  private final DeviceModelRepository deviceModelRepository;

  public MetaController(
    CategoryRepository categoryRepository,
    BrandRepository brandRepository,
    DeviceModelRepository deviceModelRepository
  ) {
    this.categoryRepository = categoryRepository;
    this.brandRepository = brandRepository;
    this.deviceModelRepository = deviceModelRepository;
  }

  /**
   * 类目列表。
   * <p>GET /api/market/categories/</p>
   */
  @GetMapping({"/categories/", "/categories"})
  @Transactional(readOnly = true)
  public List<Map<String, Object>> categories() {
    return categoryRepository.findAll().stream().map(c -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", c.getId());
      m.put("name", c.getName());
      m.put("code", c.getCode());
      return m;
    }).collect(Collectors.toList());
  }

  /**
   * 类目详情（用于按 ID 取 name）。
   * <p>GET /api/market/categories/{id}/</p>
   */
  @GetMapping({"/categories/{id}/", "/categories/{id}"})
  @Transactional(readOnly = true)
  public Map<String, Object> category(@PathVariable("id") Long id) {
    Category c = categoryRepository.findById(id).orElse(null);
    if (c == null) return null;
    Map<String, Object> m = new HashMap<>();
    m.put("id", c.getId());
    m.put("name", c.getName());
    m.put("code", c.getCode());
    return m;
  }

  /**
   * 品牌列表（可按类目过滤）。
   * <p>GET /api/market/brands/?category_id=</p>
   */
  @GetMapping({"/brands/", "/brands"})
  @Transactional(readOnly = true)
  public List<Map<String, Object>> brands(@RequestParam(value = "category_id", required = false) Long categoryId) {
    List<Brand> list = brandRepository.listByCategory(categoryId);
    return list.stream().map(b -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", b.getId());
      m.put("name", b.getName());
      if (b.getCategory() != null) m.put("category_id", b.getCategory().getId());
      return m;
    }).collect(Collectors.toList());
  }

  /**
   * 型号列表（可按类目/品牌过滤）。
   * <p>GET /api/market/device-models/?category_id=&brand_id=</p>
   */
  @GetMapping({"/device-models/", "/device-models"})
  @Transactional(readOnly = true)
  public List<Map<String, Object>> deviceModels(
    @RequestParam(value = "category_id", required = false) Long categoryId,
    @RequestParam(value = "brand_id", required = false) Long brandId
  ) {
    List<DeviceModel> list = deviceModelRepository.list(categoryId, brandId);
    return list.stream().map(dm -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", dm.getId());
      m.put("name", dm.getName());
      if (dm.getBrand() != null) m.put("brand_id", dm.getBrand().getId());
      if (dm.getCategory() != null) m.put("category_id", dm.getCategory().getId());
      return m;
    }).collect(Collectors.toList());
  }

  /**
   * 型号详情（用于按 ID 取 name）。
   * <p>GET /api/market/device-models/{id}/</p>
   */
  @GetMapping({"/device-models/{id}/", "/device-models/{id}"})
  @Transactional(readOnly = true)
  public Map<String, Object> deviceModel(@PathVariable("id") Long id) {
    DeviceModel dm = deviceModelRepository.findById(id).orElse(null);
    if (dm == null) return null;
    Map<String, Object> m = new HashMap<>();
    m.put("id", dm.getId());
    m.put("name", dm.getName());
    if (dm.getBrand() != null) {
      m.put("brand_id", dm.getBrand().getId());
      m.put("brand_name", dm.getBrand().getName());
    }
    if (dm.getCategory() != null) {
      m.put("category_id", dm.getCategory().getId());
    }
    m.put("image_url", dm.getImageUrl());
    m.put("msrp_price", dm.getMsrpPrice());
    m.put("index_type", dm.getIndexType());
    return m;
  }

  /**
   * 型号参考信息（前端已隐藏“商品参考”弹窗，但保留接口以兼容）。
   * <p>GET /api/market/device-models/reference/?category_id=&device_model_id=</p>
   */
  @GetMapping({"/device-models/reference/", "/device-models/reference"})
  @Transactional(readOnly = true)
  public Map<String, Object> deviceModelReference(
    @RequestParam("category_id") Long categoryId,
    @RequestParam("device_model_id") Long deviceModelId
  ) {
    DeviceModel dm = deviceModelRepository.findById(deviceModelId).orElse(null);
    if (dm == null) return null;
    Map<String, Object> m = new HashMap<>();
    m.put("id", dm.getId());
    m.put("name", dm.getName());
    if (dm.getBrand() != null) {
      m.put("brand_id", dm.getBrand().getId());
      m.put("brand_name", dm.getBrand().getName());
    }
    m.put("image_url", dm.getImageUrl());
    m.put("msrp_price", dm.getMsrpPrice());
    m.put("index_type", dm.getIndexType());
    return m;
  }
}
