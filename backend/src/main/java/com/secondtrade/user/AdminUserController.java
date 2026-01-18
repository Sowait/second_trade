package com.secondtrade.user;

import com.secondtrade.security.SecurityUtil;
import com.secondtrade.user.dto.UserDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/admin/users")
/**
 * 后台用户管理接口（需要管理员权限）。
 */
public class AdminUserController {
  private final UserRepository userRepository;

  public AdminUserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * 用户列表（分页/搜索）。
   * <p>GET /api/auth/admin/users/?page=&page_size=&q=</p>
   */
  @GetMapping({"/", ""})
  public Map<String, Object> list(
    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
    @RequestParam(value = "page_size", required = false, defaultValue = "10") int pageSize,
    @RequestParam(value = "q", required = false, defaultValue = "") String q
  ) {
    SecurityUtil.requireAdmin();
    int p = Math.max(1, page) - 1;
    int ps = Math.min(100, Math.max(1, pageSize));
    Page<User> result = userRepository.search(q, PageRequest.of(p, ps));
    List<UserDto> list = result.getContent().stream().map(UserDto::from).collect(Collectors.toList());
    Map<String, Object> res = new HashMap<>();
    res.put("count", result.getTotalElements());
    res.put("results", list);
    return res;
  }

  /**
   * 禁用用户。
   * <p>POST /api/auth/admin/users/{id}/disable/</p>
   */
  @PostMapping("/{id}/disable/")
  public Map<String, Object> disable(@PathVariable("id") Long id) {
    SecurityUtil.requireAdmin();
    User u = userRepository.findById(id).orElse(null);
    if (u != null) {
      u.setDisabled(true);
      userRepository.save(u);
    }
    return java.util.Collections.singletonMap("ok", true);
  }
}
