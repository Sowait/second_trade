package com.secondtrade.user;

import com.secondtrade.security.SecurityUtil;
import com.secondtrade.user.dto.LoginReq;
import com.secondtrade.user.dto.LogoutReq;
import com.secondtrade.user.dto.RefreshReq;
import com.secondtrade.user.dto.RegisterReq;
import com.secondtrade.user.dto.UpdateMeReq;
import com.secondtrade.user.dto.UserDto;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
/**
 * 鉴权与个人信息接口。
 *
 * <p>前端通过 Authorization: Bearer &lt;access_token&gt; 访问需要登录的接口。</p>
 */
public class AuthController {
  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 用户注册。
   * <p>POST /api/auth/register</p>
   */
  @PostMapping("/register")
  public Map<String, Object> register(@Valid @RequestBody RegisterReq req) {
    userService.register(req);
    return java.util.Collections.singletonMap("ok", true);
  }

  /**
   * 用户登录，返回 access/refresh 与 user 信息。
   * <p>POST /api/auth/login</p>
   */
  @PostMapping("/login")
  public Map<String, Object> login(@Valid @RequestBody LoginReq req) {
    return userService.login(req);
  }

  /**
   * 退出登录（使 refresh 失效）。
   * <p>POST /api/auth/logout</p>
   */
  @PostMapping("/logout")
  public Map<String, Object> logout(@Valid @RequestBody LogoutReq req) {
    userService.logout(req.getRefresh());
    return java.util.Collections.singletonMap("ok", true);
  }

  /**
   * 刷新 access token。
   * <p>POST /api/auth/refresh</p>
   */
  @PostMapping("/refresh")
  public Map<String, Object> refresh(@Valid @RequestBody RefreshReq req) {
    return userService.refresh(req.getRefresh());
  }

  /**
   * 获取当前登录用户信息。
   * <p>GET /api/auth/me （兼容 /api/auth/me/）</p>
   */
  @GetMapping({"/me", "/me/"})
  public UserDto me() {
    return UserDto.from(SecurityUtil.requireUser());
  }

  /**
   * 更新当前登录用户信息（昵称/邮箱/电话/地址/学校/密码）。
   * <p>PUT /api/auth/me （兼容 /api/auth/me/）</p>
   */
  @PutMapping({"/me", "/me/"})
  public UserDto updateMe(@RequestBody UpdateMeReq req) {
    return UserDto.from(userService.updateMe(SecurityUtil.requireUser(), req));
  }
}
