package com.secondtrade.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
  @Value("${app.upload.base-dir:./uploads}")
  private String uploadBaseDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path base1 = resolveUploadBaseDir(uploadBaseDir);
    Path base2 = resolveParentUploadBaseDir(uploadBaseDir);

    String location1 = toLocation(base1);
    if (base2 != null && !base2.equals(base1)) {
      String location2 = toLocation(base2);
      registry.addResourceHandler("/media/**").addResourceLocations(location1, location2);
      return;
    }
    registry.addResourceHandler("/media/**").addResourceLocations(location1);
  }

  private static Path resolveUploadBaseDir(String rawDir) {
    Path raw = Paths.get(rawDir);
    if (raw.isAbsolute()) return raw.toAbsolutePath().normalize();
    Path cand = raw.toAbsolutePath().normalize();
    Path parentCand = resolveParentUploadBaseDir(rawDir);
    if (Files.exists(cand.resolve("drafts")) || Files.exists(cand.resolve("products"))) return cand;
    if (parentCand != null && (Files.exists(parentCand.resolve("drafts")) || Files.exists(parentCand.resolve("products")))) {
      return parentCand;
    }
    return cand;
  }

  private static Path resolveParentUploadBaseDir(String rawDir) {
    Path raw = Paths.get(rawDir);
    if (raw.isAbsolute()) return null;
    Path cwd = Paths.get("").toAbsolutePath().normalize();
    Path parent = cwd.getParent();
    if (parent == null) return null;
    return parent.resolve(raw).normalize();
  }

  private static String toLocation(Path base) {
    String location = base.toUri().toString();
    if (!location.endsWith("/")) location = location + "/";
    return location;
  }
}
