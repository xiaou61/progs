package com.campus.competition.modules.submission.service;

import com.campus.competition.modules.submission.model.SubmissionFileUploadResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionFileStorageService {

  private final String storageDirectory;

  public SubmissionFileStorageService(
    @Value("${campus.storage.submissions-dir:local-storage/submissions}") String storageDirectory
  ) {
    this.storageDirectory = storageDirectory;
  }

  public SubmissionFileUploadResult store(MultipartFile file) {
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
      throw new IllegalStateException("上传文件保存失败", exception);
    }

    return new SubmissionFileUploadResult(
      originalFileName,
      "/uploads/submissions/" + storedFileName,
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
