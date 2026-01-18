package com.secondtrade.market.storage;

import com.secondtrade.common.ApiException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
  private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
  private final Path baseDir;

  public FileStorageService(@Value("${app.upload.base-dir:./uploads}") String baseDir) {
    this.baseDir = resolveUploadBaseDir(baseDir);
  }

  private static Path resolveUploadBaseDir(String rawDir) {
    Path raw = Paths.get(rawDir);
    if (raw.isAbsolute()) return raw.toAbsolutePath().normalize();
    Path cand1 = raw.toAbsolutePath().normalize();

    Path cwd = Paths.get("").toAbsolutePath().normalize();
    Path parent = cwd.getParent();
    Path cand2 = parent == null ? null : parent.resolve(raw).normalize();

    if (Files.exists(cand1.resolve("drafts")) || Files.exists(cand1.resolve("products"))) return cand1;
    if (cand2 != null && (Files.exists(cand2.resolve("drafts")) || Files.exists(cand2.resolve("products")))) return cand2;
    if (Files.exists(cand1)) return cand1;
    if (cand2 != null && Files.exists(cand2)) return cand2;
    return cand1;
  }

  public String saveDraftImage(String draftKey, MultipartFile file, int sortOrder) {
    if (file == null || file.isEmpty()) throw new ApiException(400, "图片缺失");
    String original = file.getOriginalFilename();
    String ext = "";
    if (StringUtils.hasText(original) && original.contains(".")) {
      ext = original.substring(original.lastIndexOf(".")).toLowerCase();
    }
    String name = UUID.randomUUID().toString().replace("-", "") + ext;
    Path dir = baseDir.resolve("drafts").resolve(draftKey);
    try {
      Files.createDirectories(dir);
      Path target = dir.resolve(name);
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      log.error("Save draft image failed: draftKey={}, baseDir={}, targetDir={}", draftKey, baseDir, dir, e);
      throw new ApiException(500, "保存图片失败");
    }
    return "/media/drafts/" + draftKey + "/" + name;
  }
}
