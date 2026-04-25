package com.aitutor.api.storage;

import com.aitutor.api.config.AiTutorProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final Path localRoot;

    public StorageService(AiTutorProperties properties) {
        this.localRoot = Path.of(properties.storage().localRoot()).toAbsolutePath().normalize();
    }

    public StoredFile save(MultipartFile file, String category) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        try {
            Files.createDirectories(localRoot.resolve(category));
            String originalFilename = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
            String extension = extensionOf(originalFilename);
            String objectKey = category + "/" + UUID.randomUUID() + extension;
            Path target = localRoot.resolve(objectKey).normalize();
            file.transferTo(target);
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
