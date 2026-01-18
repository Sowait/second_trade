package com.secondtrade.market.product;

import com.secondtrade.market.category.Category;
import com.secondtrade.market.common.StringListJsonConverter;
import com.secondtrade.market.device.DeviceModel;
import com.secondtrade.user.User;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "market_products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User seller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_model_id")
  private DeviceModel deviceModel;

  @Column(length = 255)
  private String title;

  @Column(length = 128)
  private String school;

  @Column(name = "product_summary", length = 1000)
  private String productSummary;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "selling_price")
  private Integer sellingPrice;

  @Column(name = "original_price")
  private Integer originalPrice;

  @Column(name = "years_used")
  private Integer yearsUsed;

  @Column(name = "grade_label", length = 32)
  private String gradeLabel;

  @Convert(converter = StringListJsonConverter.class)
  @Column(columnDefinition = "text")
  private List<String> defects;

  @Column(name = "main_image", length = 255)
  private String mainImage;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 32)
  private ProductStatus status;

  @Column(name = "review_reject_reason", length = 255)
  private String reviewRejectReason;

  @Column(name = "favorite_count")
  private Integer favoriteCount;

  @Column(name = "view_count")
  private Integer viewCount;

  @Column(name = "market_tag", length = 64)
  private String marketTag;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getSeller() {
    return seller;
  }

  public void setSeller(User seller) {
    this.seller = seller;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public DeviceModel getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(DeviceModel deviceModel) {
    this.deviceModel = deviceModel;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSchool() {
    return school;
  }

  public void setSchool(String school) {
    this.school = school;
  }

  public String getProductSummary() {
    return productSummary;
  }

  public void setProductSummary(String productSummary) {
    this.productSummary = productSummary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getSellingPrice() {
    return sellingPrice;
  }

  public void setSellingPrice(Integer sellingPrice) {
    this.sellingPrice = sellingPrice;
  }

  public Integer getOriginalPrice() {
    return originalPrice;
  }

  public void setOriginalPrice(Integer originalPrice) {
    this.originalPrice = originalPrice;
  }

  public Integer getYearsUsed() {
    return yearsUsed;
  }

  public void setYearsUsed(Integer yearsUsed) {
    this.yearsUsed = yearsUsed;
  }

  public String getGradeLabel() {
    return gradeLabel;
  }

  public void setGradeLabel(String gradeLabel) {
    this.gradeLabel = gradeLabel;
  }

  public List<String> getDefects() {
    return defects;
  }

  public void setDefects(List<String> defects) {
    this.defects = defects;
  }

  public String getMainImage() {
    return mainImage;
  }

  public void setMainImage(String mainImage) {
    this.mainImage = mainImage;
  }

  public ProductStatus getStatus() {
    return status;
  }

  public void setStatus(ProductStatus status) {
    this.status = status;
  }

  public String getReviewRejectReason() {
    return reviewRejectReason;
  }

  public void setReviewRejectReason(String reviewRejectReason) {
    this.reviewRejectReason = reviewRejectReason;
  }

  public Integer getFavoriteCount() {
    return favoriteCount;
  }

  public void setFavoriteCount(Integer favoriteCount) {
    this.favoriteCount = favoriteCount;
  }

  public Integer getViewCount() {
    return viewCount;
  }

  public void setViewCount(Integer viewCount) {
    this.viewCount = viewCount;
  }

  public String getMarketTag() {
    return marketTag;
  }

  public void setMarketTag(String marketTag) {
    this.marketTag = marketTag;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}

