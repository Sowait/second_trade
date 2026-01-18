package com.secondtrade.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final AuthUserService authUserService;

  public JwtAuthFilter(JwtUtil jwtUtil, AuthUserService authUserService) {
    this.jwtUtil = jwtUtil;
    this.authUserService = authUserService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = header.substring("Bearer ".length()).trim();
    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }
    try {
      Long uid = jwtUtil.parseUserId(token);
      if (uid == null) {
        filterChain.doFilter(request, response);
        return;
      }
      AuthUserDetails details = authUserService.loadByUserId(uid);
      if (details == null) {
        filterChain.doFilter(request, response);
        return;
      }
      UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception ignored) {
    }
    filterChain.doFilter(request, response);
  }
}
