package com.secondtrade.user.dto;

import com.secondtrade.user.User;
import java.time.LocalDateTime;

public class UserDto {
  private Long id;
  private String username;
  private String student_id;
  private String email;
  private String phone;
  private String school;
  private String role;
  private String nickname;
  private String avatar;
  private String address;
  private Integer credit_score;
  private boolean is_disabled;
  private boolean is_superuser;
  private boolean is_staff;
  private LocalDateTime created_at;

  public static UserDto from(User u) {
    UserDto d = new UserDto();
    d.id = u.getId();
    d.username = u.getUsername();
    d.student_id = u.getStudentId();
    d.email = u.getEmail();
    d.phone = u.getPhone();
    d.school = u.getSchool();
    d.role = u.getRole();
    d.nickname = u.getNickname();
    d.avatar = u.getAvatar();
    d.address = u.getAddress();
    d.credit_score = u.getCreditScore();
    d.is_disabled = u.isDisabled();
    d.is_superuser = u.isSuperuser();
    d.is_staff = u.isStaff();
    d.created_at = u.getCreatedAt();
    return d;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getStudent_id() {
    return student_id;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getSchool() {
    return school;
  }

  public String getRole() {
    return role;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAvatar() {
    return avatar;
  }

  public String getAddress() {
    return address;
  }

  public Integer getCredit_score() {
    return credit_score;
  }

  public boolean isIs_disabled() {
    return is_disabled;
  }

  public boolean isIs_superuser() {
    return is_superuser;
  }

  public boolean isIs_staff() {
    return is_staff;
  }

  public LocalDateTime getCreated_at() {
    return created_at;
  }
}

