package top.cch.file.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "file", name = "storage-type", havingValue = "oss")
public class AliyunOssConfig {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(FileProperties properties) {
        FileProperties.Oss oss = properties.getOss();
        requireText(oss.getEndpoint(), "file.oss.endpoint");
        requireText(oss.getAccessKeyId(), "file.oss.access-key-id");
        requireText(oss.getAccessKeySecret(), "file.oss.access-key-secret");
        requireText(oss.getBucketName(), "file.oss.bucket-name");
        return new OSSClientBuilder().build(oss.getEndpoint(), oss.getAccessKeyId(), oss.getAccessKeySecret());
    }

    private void requireText(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "storage-type 设为 oss，但 " + key + " 没有配置；换成本地存储请把 file.storage-type 改成 local");
        }
    }
}
