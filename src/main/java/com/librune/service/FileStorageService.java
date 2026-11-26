package com.librune.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded book cover images on disk (persisted via Docker volume) and returns public /uploads paths.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path booksUploadDir;

    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) throws IOException {
        this.booksUploadDir = Paths.get(uploadDir, "books").toAbsolutePath().normalize();
        Files.createDirectories(this.booksUploadDir);
    }

    public String storeBookCover(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String ext = StringUtils.getFilenameExtension(original);
        String normalizedExt = ext != null ? ext.toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(normalizedExt)) {
            throw new IllegalArgumentException("Chỉ hỗ trợ định dạng ảnh: jpg, jpeg, png, webp");
        }
        String filename = UUID.randomUUID() + "." + normalizedExt;
        Path target = this.booksUploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/books/" + filename;
    }
}
