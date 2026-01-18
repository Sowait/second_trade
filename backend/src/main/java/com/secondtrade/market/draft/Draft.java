package com.secondtrade.market.draft;

import com.secondtrade.user.User;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "market_drafts")
public class Draft {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "draft_key", unique = true, length = 64)
  private String draftKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User seller;

  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "device_model_id")
  private Long deviceModelId;

  @Column(name = "years_used")
  private Integer yearsUsed;

  @Column(name = "original_price")
  private Integer originalPrice;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDraftKey() {
    return draftKey;
  }

  public void setDraftKey(String draftKey) {
    this.draftKey = draftKey;
  }

  public User getSeller() {
    return seller;
  }

  public void setSeller(User seller) {
    this.seller = seller;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public Long getDeviceModelId() {
    return deviceModelId;
  }

  public void setDeviceModelId(Long deviceModelId) {
    this.deviceModelId = deviceModelId;
  }

  public Integer getYearsUsed() {
    return yearsUsed;
  }

  public void setYearsUsed(Integer yearsUsed) {
    this.yearsUsed = yearsUsed;
  }

  public Integer getOriginalPrice() {
    return originalPrice;
  }

  public void setOriginalPrice(Integer originalPrice) {
    this.originalPrice = originalPrice;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}

