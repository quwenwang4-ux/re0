package com.seafish.service;

import com.seafish.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_IMAGE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final Path storageRoot;

    public FileStorageService(
            @Value("${seafish.storage.root:./uploads}")
            String storageRoot
    ) {
        this.storageRoot = Path.of(storageRoot)
                .toAbsolutePath()
                .normalize();
    }

    public StoredFile saveDetectionImage(
            MultipartFile file
    ) {
        validateImage(file);

        String extension = extensionOf(
                file.getOriginalFilename()
        );
        String storedName = UUID.randomUUID() + extension;
        Path directory = storageRoot
                .resolve("detection")
                .normalize();
        Path target = directory
                .resolve(storedName)
                .normalize();

        if (!target.startsWith(directory)) {
            throw new BusinessException(40020, "文件名不合法");
        }

        try {
            Files.createDirectories(directory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException exception) {
            throw new BusinessException(
                    50020,
                    "图片保存失败，请稍后重试"
            );
        }

        return new StoredFile(
                target,
                "detection/" + storedName,
                file.getOriginalFilename(),
                file.getContentType()
        );
    }

    public Resource loadPrivateFile(String storageKey) {
        Path target = resolveStorageKey(storageKey);
        try {
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(40420, "图片文件不存在");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new BusinessException(40420, "图片文件不存在");
        }
    }

    public String detectContentType(String storageKey) {
        try {
            String type = Files.probeContentType(
                    resolveStorageKey(storageKey)
            );
            return type == null
                    ? "application/octet-stream"
                    : type;
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    private Path resolveStorageKey(String storageKey) {
        Path target = storageRoot
                .resolve(storageKey)
                .normalize();

        if (!target.startsWith(storageRoot)) {
            throw new BusinessException(40320, "无权访问该文件");
        }
        return target;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40020, "请选择需要识别的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(40021, "图片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !ALLOWED_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
        )) {
            throw new BusinessException(
                    40022,
                    "仅支持JPG、PNG或WEBP图片"
            );
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return ".img";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return ".img";
        }

        String extension = fileName.substring(dotIndex)
                .toLowerCase(Locale.ROOT);
        return switch (extension) {
            case ".jpg", ".jpeg", ".png", ".webp" -> extension;
            default -> ".img";
        };
    }

    public record StoredFile(
            Path path,
            String storageKey,
            String originalFileName,
            String contentType
    ) {
    }
}
