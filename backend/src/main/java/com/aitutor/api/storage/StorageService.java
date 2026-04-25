package com.aitutor.api.storage;

import com.aitutor.api.config.AiTutorProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final Path localRoot;

    public StorageService(AiTutorProperties properties) {
        this.localRoot = Path.of(properties.storage().localRoot()).toAbsolutePath().normalize();
        log.info("local file storage root={}", localRoot);
    }

    public StoredFile save(MultipartFile file, UUID studentId, String category) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        try {
            String studentDirectory = studentId.toString();
            Path categoryRoot = localRoot.resolve(studentDirectory).resolve(category).normalize();
            if (!categoryRoot.startsWith(localRoot)) {
                throw new IllegalArgumentException("Invalid storage path");
            }
            Files.createDirectories(categoryRoot);
            String originalFilename = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
            String extension = extensionOf(originalFilename);
            String objectKey = studentDirectory + "/" + category + "/" + UUID.randomUUID() + extension;
            Path target = localRoot.resolve(objectKey).normalize();
            if (!target.startsWith(localRoot)) {
                throw new IllegalArgumentException("Invalid object key");
            }
            file.transferTo(target);
            log.info(
                    "stored uploaded file studentId={} category={} objectKey={} path={} contentType={} sizeBytes={}",
                    studentId,
                    category,
                    objectKey,
                    target,
                    file.getContentType(),
                    file.getSize()
            );
            return new StoredFile(objectKey, target.toString(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file");
        }
    }

    public Path resolve(String objectKey) {
        Path resolved = localRoot.resolve(objectKey).normalize();
        if (!resolved.startsWith(localRoot)) {
            throw new IllegalArgumentException("Invalid object key");
        }
        return resolved;
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot);
    }
}
