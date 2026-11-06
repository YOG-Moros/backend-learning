package top.cch.file.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.cch.file.common.BusinessException;
import top.cch.file.common.ErrorCode;
import top.cch.file.config.FileProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(prefix = "file", name = "storage-type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final FileProperties properties;

    public LocalStorageService(FileProperties properties) {
        this.properties = properties;
    }

    @Override
    public String type() {
        return "local";
    }

    @Override
    public void write(String objectKey, MultipartFile file) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR, "写入本地文件失败");
        }
    }

    @Override
    public StoredObject read(String objectKey) {
        Path target = resolve(objectKey);
        if (!Files.isRegularFile(target)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        try {
            long size = Files.size(target);
            String type = org.springframework.http.MediaTypeFactory.getMediaType(target.getFileName().toString())
                    .map(Object::toString).orElse(StoredObject.DEFAULT_CONTENT_TYPE);
            InputStream stream = Files.newInputStream(target);
            return new StoredObject(objectKey, size, type, stream);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR, "读取本地文件失败");
        }
    }

    @Override
    public boolean exists(String objectKey) {
        return Files.isRegularFile(resolve(objectKey));
    }

    @Override
    public void delete(String objectKey) {
        Path target = resolve(objectKey);
        try {
            Files.deleteIfExists(target);
            removeEmptyParents(target.getParent());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR, "删除本地文件失败");
        }
    }

    @Override
    public String url(String objectKey) {
        return properties.getLocal().getUrlPrefix() + "/" + objectKey;
    }

    private Path resolve(String objectKey) {
        Path base = baseDir();
        Path target = base.resolve(objectKey).normalize();
        if (!target.startsWith(base) || target.equals(base)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的文件路径");
        }
        for (Path part = target; part != null && part.startsWith(base); part = part.getParent()) {
            if (Files.isSymbolicLink(part)) throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的文件路径");
        }
        return target;
    }

    private Path baseDir() {
        return Paths.get(properties.getLocal().getBaseDir()).toAbsolutePath().normalize();
    }

    private void removeEmptyParents(Path dir) {
        Path base = baseDir();
        Path current = dir;
        while (current != null && current.startsWith(base) && !current.equals(base)) {
            try (Stream<Path> children = Files.list(current)) {
                if (children.findAny().isPresent()) {
                    return;
                }
                Files.delete(current);
                current = current.getParent();
            } catch (IOException e) {
                return;
            }
        }
    }
}
