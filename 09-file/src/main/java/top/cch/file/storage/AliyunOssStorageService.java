package top.cch.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.cch.file.common.BusinessException;
import top.cch.file.common.ErrorCode;
import top.cch.file.config.FileProperties;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;

@Service
@ConditionalOnProperty(prefix = "file", name = "storage-type", havingValue = "oss")
public class AliyunOssStorageService implements StorageService {

    private static final String NO_SUCH_KEY = "NoSuchKey";

    private final OSS ossClient;
    private final FileProperties properties;

    public AliyunOssStorageService(OSS ossClient, FileProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    @Override
    public String type() {
        return "oss";
    }

    @Override
    public void write(String objectKey, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            metadata.setContentType(file.getContentType());
        }
        metadata.setContentDisposition("attachment");
        try (InputStream in = file.getInputStream()) {
            ossClient.putObject(bucket(), fullKey(objectKey), in, metadata);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR, "写入对象存储失败");
        }
    }

    @Override
    public StoredObject read(String objectKey) {
        try {
            OSSObject object = ossClient.getObject(bucket(), fullKey(objectKey));
            String contentType = object.getObjectMetadata().getContentType();
            if (contentType == null) {
                contentType = StoredObject.DEFAULT_CONTENT_TYPE;
            }
            return new StoredObject(objectKey, object.getObjectMetadata().getContentLength(),
                    contentType, object.getObjectContent());
        } catch (OSSException e) {
            if (NO_SUCH_KEY.equals(e.getErrorCode())) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.STORAGE_ERROR, "读取对象存储失败");
        }
    }

    @Override
    public boolean exists(String objectKey) {
        return ossClient.doesObjectExist(bucket(), fullKey(objectKey));
    }

    @Override
    public void delete(String objectKey) {
        ossClient.deleteObject(bucket(), fullKey(objectKey));
    }

    @Override
    public String url(String objectKey) {
        String domain = properties.getOss().getDomain();
        if (StringUtils.hasText(domain)) {
            return trimSlash(domain) + "/" + fullKey(objectKey);
        }
        String host = trimSlash(properties.getOss().getEndpoint());
        host = host.replace("https://", "").replace("http://", "");
        return "https://" + bucket() + "." + host + "/" + fullKey(objectKey);
    }

    @Override
    public String presignedUrl(String objectKey, Duration ttl) {
        Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());
        return ossClient.generatePresignedUrl(bucket(), fullKey(objectKey), expiration).toString();
    }

    private String bucket() {
        return properties.getOss().getBucketName();
    }

    private String fullKey(String objectKey) {
        String dir = properties.getOss().getDir();
        if (StringUtils.hasText(dir)) {
            return trimSlash(dir) + "/" + objectKey;
        }
        return objectKey;
    }

    private String trimSlash(String value) {
        String result = value;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
