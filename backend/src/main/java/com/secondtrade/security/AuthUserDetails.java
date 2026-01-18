package com.secondtrade.security;

import com.secondtrade.user.User;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUserDetails implements UserDetails {
  private final User user;

  public AuthUserDetails(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  @Override
  public List<? extends GrantedAuthority> getAuthorities() {
    String role = user.getRole() == null ? "user" : user.getRole();
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !user.isDisabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return !user.isDisabled();
  }
}

