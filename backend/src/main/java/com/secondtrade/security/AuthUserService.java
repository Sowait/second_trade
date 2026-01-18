package com.secondtrade.security;

import com.secondtrade.user.User;
import com.secondtrade.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService {
  private final UserRepository userRepository;

  public AuthUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public AuthUserDetails loadByUserId(Long userId) {
    User u = userRepository.findById(userId).orElse(null);
    if (u == null) return null;
    return new AuthUserDetails(u);
  }
}

