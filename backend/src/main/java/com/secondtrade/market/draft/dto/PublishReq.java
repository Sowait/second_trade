package com.secondtrade.market.draft.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PublishReq {
  @NotNull
  private Long category_id;
  @NotNull
  private Long device_model_id;
  @NotNull
  private Integer years_used;
  @NotNull
  private Integer original_price;

  @NotBlank
  private String title;
  private String school;
  @NotBlank
  private String product_summary;
  @NotBlank
  private String description;
  @NotNull
  private Integer selling_price;

  public Long getCategory_id() {
    return category_id;
  }

  public void setCategory_id(Long category_id) {
    this.category_id = category_id;
  }

  public Long getDevice_model_id() {
    return device_model_id;
  }

  public void setDevice_model_id(Long device_model_id) {
    this.device_model_id = device_model_id;
  }

  public Integer getYears_used() {
    return years_used;
  }

  public void setYears_used(Integer years_used) {
    this.years_used = years_used;
  }

  public Integer getOriginal_price() {
    return original_price;
  }

  public void setOriginal_price(Integer original_price) {
    this.original_price = original_price;
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

  public String getProduct_summary() {
    return product_summary;
  }

  public void setProduct_summary(String product_summary) {
    this.product_summary = product_summary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getSelling_price() {
    return selling_price;
  }

  public void setSelling_price(Integer selling_price) {
    this.selling_price = selling_price;
  }
}

