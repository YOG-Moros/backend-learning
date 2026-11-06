package top.cch.file.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface StorageService {

    String type();

    void write(String objectKey, MultipartFile file);

    StoredObject read(String objectKey);

    boolean exists(String objectKey);

    void delete(String objectKey);

    String url(String objectKey);

    default String presignedUrl(String objectKey, Duration ttl) {
        return url(objectKey);
    }
}
