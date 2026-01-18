package com.secondtrade.user;

import com.secondtrade.common.ApiException;
import com.secondtrade.security.JwtUtil;
import com.secondtrade.user.dto.LoginReq;
import com.secondtrade.user.dto.RegisterReq;
import com.secondtrade.user.dto.UpdateMeReq;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final AuthTokenRepository authTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public UserService(
    UserRepository userRepository,
    AuthTokenRepository authTokenRepository,
    PasswordEncoder passwordEncoder,
    JwtUtil jwtUtil
  ) {
    this.userRepository = userRepository;
    this.authTokenRepository = authTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  @Transactional
  public void register(RegisterReq req) {
    String sid = StringUtils.trimWhitespace(req.getStudent_id());
    String username = StringUtils.trimWhitespace(req.getUsername());
    if (!StringUtils.hasText(sid) || !StringUtils.hasText(username)) throw new ApiException(400, "参数缺失");
    if (userRepository.findByStudentId(sid).isPresent()) throw new ApiException(400, "学号已存在");
    if (userRepository.findByUsername(username).isPresent()) throw new ApiException(400, "用户名已存在");
    User u = new User();
    u.setStudentId(sid);
    u.setUsername(username);
    u.setSchool(req.getSchool());
    u.setEmail(req.getEmail());
    u.setPhone(req.getPhone());
    u.setRole("user");
    u.setNickname(username);
    u.setCreditScore(90);
    u.setDisabled(false);
    u.setSuperuser(false);
    u.setStaff(false);
    u.setCreatedAt(LocalDateTime.now());
    u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
    userRepository.save(u);
  }

  @Transactional
  public Map<String, Object> login(LoginReq req) {
    String sid = StringUtils.trimWhitespace(req.getStudent_id());
    String username = StringUtils.trimWhitespace(req.getUsername());
    User u = null;
    if (StringUtils.hasText(sid)) {
      u = userRepository.findByStudentId(sid).orElse(null);
    }
    if (u == null && StringUtils.hasText(username)) {
      u = userRepository.findByUsername(username).orElse(null);
    }
    if (u == null) throw new ApiException(400, "账号或密码错误");
    if (u.isDisabled()) throw new ApiException(403, "账号已被禁用");
    if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) throw new ApiException(400, "账号或密码错误");

    String access = jwtUtil.issueAccessToken(u.getId(), u.getRole(), u.isSuperuser() || "admin".equalsIgnoreCase(u.getRole()));
    String refresh = jwtUtil.issueRefreshToken(u.getId());

    AuthToken t = new AuthToken();
    t.setUser(u);
    t.setRefreshToken(refresh);
    t.setRevoked(false);
    t.setCreatedAt(LocalDateTime.now());
    authTokenRepository.save(t);

    Map<String, Object> res = new HashMap<>();
    res.put("access", access);
    res.put("refresh", refresh);
    res.put("user", com.secondtrade.user.dto.UserDto.from(u));
    return res;
  }

  @Transactional
  public Map<String, Object> refresh(String refreshToken) {
    AuthToken t = authTokenRepository.findByRefreshToken(refreshToken).orElse(null);
    if (t == null || t.isRevoked()) throw new ApiException(401, "refresh 无效");
    User u = t.getUser();
    if (u == null) throw new ApiException(401, "refresh 无效");
    if (u.isDisabled()) throw new ApiException(403, "账号已被禁用");
    String access = jwtUtil.issueAccessToken(u.getId(), u.getRole(), u.isSuperuser() || "admin".equalsIgnoreCase(u.getRole()));
    Map<String, Object> res = new HashMap<>();
    res.put("access", access);
    return res;
  }

  @Transactional
  public void logout(String refreshToken) {
    AuthToken t = authTokenRepository.findByRefreshToken(refreshToken).orElse(null);
    if (t != null) {
      t.setRevoked(true);
      authTokenRepository.save(t);
    }
  }

  @Transactional
  public User updateMe(User u, UpdateMeReq req) {
    if (req.getNickname() != null) u.setNickname(req.getNickname());
    if (req.getEmail() != null) u.setEmail(req.getEmail());
    if (req.getPhone() != null) u.setPhone(req.getPhone());
    if (req.getAddress() != null) u.setAddress(req.getAddress());
    if (req.getSchool() != null) u.setSchool(req.getSchool());
    if (StringUtils.hasText(req.getPassword())) {
      u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
    }
    return userRepository.save(u);
  }
}

