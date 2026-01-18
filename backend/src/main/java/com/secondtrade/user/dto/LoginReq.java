package com.secondtrade.user.dto;

import javax.validation.constraints.NotBlank;

public class LoginReq {
  private String student_id;
  private String username;
  @NotBlank
  private String password;

  public String getStudent_id() {
    return student_id;
  }

  public void setStudent_id(String student_id) {
    this.student_id = student_id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

