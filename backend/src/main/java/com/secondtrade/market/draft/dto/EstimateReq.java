package com.secondtrade.market.draft.dto;

import javax.validation.constraints.NotNull;

public class EstimateReq {
  @NotNull
  private Long category_id;
  @NotNull
  private Integer years_used;
  @NotNull
  private Integer original_price;

  public Long getCategory_id() {
    return category_id;
  }

  public void setCategory_id(Long category_id) {
    this.category_id = category_id;
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
}

