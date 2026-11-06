package top.cch.file.service;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import top.cch.file.common.BusinessException;
import top.cch.file.common.ErrorCode;
import top.cch.file.config.FileProperties;
import top.cch.file.dto.FileUploadResult;
import top.cch.file.storage.StoredObject;
import top.cch.file.storage.StorageService;
import top.cch.file.support.FileTypeDetector;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileService {

    private static final Pattern DIR_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");
    private static final String DEFAULT_DIR = "files";

    private final FileProperties properties;
    private final StorageService storageService;

    public FileService(FileProperties properties, StorageService storageService) {
        this.properties = properties;
        this.storageService = storageService;
    }

    public FileUploadResult upload(MultipartFile file, String dir) {
        validate(file);
        String objectKey = buildObjectKey(file, dir);
        storageService.write(objectKey, file);
        String contentType = file.getContentType() == null
                ? StoredObject.DEFAULT_CONTENT_TYPE
                : file.getContentType();
        return new FileUploadResult(objectKey, file.getOriginalFilename(), storageService.url(objectKey),
                file.getSize(), contentType, storageService.type());
    }

    public FileUploadResult uploadImage(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) throw new BusinessException(ErrorCode.FILE_EMPTY);
        if (!FileTypeDetector.isImageExtension(extensionOf(file))) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "只允许上传图片文件");
        }
        return upload(file, dir);
    }

    public List<FileUploadResult> uploadBatch(MultipartFile[] files, String dir) {
        if (files == null || files.length == 0) throw new BusinessException(ErrorCode.FILE_EMPTY);
        List<FileUploadResult> results = new ArrayList<>(files.length);
        try {
            for (MultipartFile file : files) {
                results.add(upload(file, dir));
            }
            return results;
        } catch (RuntimeException e) {
            results.forEach(r -> {
                try { storageService.delete(r.objectKey()); }
                catch (RuntimeException cleanup) { e.addSuppressed(cleanup); }
            });
            throw e;
        }
    }

    public StoredObject read(String objectKey) {
        return storageService.read(objectKey);
    }

    public void delete(String objectKey) {
        storageService.delete(objectKey);
    }

    public String url(String objectKey, Duration ttl) {
        if (ttl.isNegative() || ttl.isZero() || ttl.compareTo(Duration.ofDays(7)) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "访问地址有效期必须在 1 分钟至 7 天之间");
        }
        return storageService.presignedUrl(objectKey, ttl);
    }

    public String storageType() {
        return storageService.type();
    }

    public String normalizeKey(String key) {
        String objectKey = key.startsWith("/") ? key.substring(1) : key;
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("文件 key 不能为空");
        }
        return objectKey;
    }

    public ResponseEntity<StreamingResponseBody> stream(StoredObject object, String fileName, boolean attachment) {
        ContentDisposition disposition = (attachment ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        StreamingResponseBody body = out -> {
            try (object) {
                object.stream().transferTo(out);
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(object.contentType()))
                .contentLength(object.size())
                .body(body);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > properties.getMaxFileSize().toBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE,
                    "文件不能超过 " + properties.getMaxFileSize().toMegabytes() + "MB");
        }
        String extension = extensionOf(file);
        if (!properties.getAllowedExtensions().contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不支持的扩展名：" + extension);
        }
        String realType = FileTypeDetector.detect(file);
        if (FileTypeDetector.isExecutable(realType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不允许上传可执行文件");
        }
        if (FileTypeDetector.isImageExtension(extension) && !FileTypeDetector.matchesImage(extension, realType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "文件内容与扩展名不符，实际类型：" + realType);
        }
    }

    private String extensionOf(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName) || !originalName.contains(".")) {
            return "";
        }
        String name = originalName.substring(originalName.lastIndexOf('/') + 1);
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

    private String buildObjectKey(MultipartFile file, String dir) {
        String folder = StringUtils.hasText(dir) ? dir.trim() : DEFAULT_DIR;
        if (!DIR_PATTERN.matcher(folder).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "目录只能是字母、数字、下划线或连字符");
        }
        DateTimeFormatter formatter = properties.dateFormatter();
        String date = LocalDate.now().format(formatter);
        return folder + "/" + date + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extensionOf(file);
    }
}
