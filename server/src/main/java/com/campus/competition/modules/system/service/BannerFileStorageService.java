package com.campus.competition.modules.system.service;

import com.campus.competition.modules.system.model.BannerFileUploadResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BannerFileStorageService {

  private final String storageDirectory;

  public BannerFileStorageService(
    @Value("${campus.storage.banners-dir:local-storage/banners}") String storageDirectory
  ) {
    this.storageDirectory = storageDirectory;
  }

  public BannerFileUploadResult store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("上传文件不能为空");
    }

    String originalFileName = file.getOriginalFilename();
    if (originalFileName == null || originalFileName.isBlank()) {
      throw new IllegalArgumentException("文件名不能为空");
    }

    String extension = extractExtension(originalFileName);
    String storedFileName = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "") + extension;
    Path targetDirectory = getStoragePath();

    try {
      Files.createDirectories(targetDirectory);
      file.transferTo(targetDirectory.resolve(storedFileName));
    } catch (IOException exception) {
      throw new IllegalStateException("上传图片保存失败", exception);
    }

    return new BannerFileUploadResult(
      originalFileName,
      "/uploads/banners/" + storedFileName,
      file.getSize()
    );
  }

  public String getResourceLocation() {
    String uri = getStoragePath().toUri().toString();
    return uri.endsWith("/") ? uri : uri + "/";
  }

  private Path getStoragePath() {
    return Path.of(storageDirectory).toAbsolutePath().normalize();
  }

  private String extractExtension(String originalFileName) {
    int extensionIndex = originalFileName.lastIndexOf('.');
    if (extensionIndex < 0) {
      return "";
    }
    return originalFileName.substring(extensionIndex);
  }
}
