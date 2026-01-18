package com.secondtrade.security;

import com.secondtrade.common.ApiException;
import com.secondtrade.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
  private SecurityUtil() {}

  public static User requireUser() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || !(a.getPrincipal() instanceof AuthUserDetails)) {
      throw new ApiException(401, "未登录");
    }
    AuthUserDetails d = (AuthUserDetails) a.getPrincipal();
    if (d == null || d.getUser() == null) throw new ApiException(401, "未登录");
    if (d.getUser().isDisabled()) throw new ApiException(403, "账号已被禁用");
    return d.getUser();
  }

  public static void requireAdmin() {
    User u = requireUser();
    boolean ok = "admin".equalsIgnoreCase(u.getRole()) || u.isSuperuser() || u.isStaff();
    if (!ok) throw new ApiException(403, "forbidden");
  }
}

