package com.secondtrade.user.dto;

import javax.validation.constraints.NotBlank;

public class RefreshReq {
  @NotBlank
  private String refresh;

  public String getRefresh() {
    return refresh;
  }

  public void setRefresh(String refresh) {
    this.refresh = refresh;
  }
}

