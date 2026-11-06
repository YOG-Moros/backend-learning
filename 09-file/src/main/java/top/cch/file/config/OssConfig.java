package top.cch.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 阿里云 OSS 连接配置
 *
 * <p>密钥类配置单独放在 aliyun-oss.properties 里，不和业务配置混在一起，
 * 该文件已加入 .gitignore，不会提交到仓库。</p>
 *
 * @author cch
 * @date 2026/11/4
 * @description 作业要求 1：编写配置
 */
@Data
@Configuration
@PropertySource(value = {"classpath:aliyun-oss.properties", "file:./aliyun-oss.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "aliyun-oss")
public class OssConfig {

    /** 服务节点，如 oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    /** 存储桶名称 */
    private String bucket;

    /** 上传目录，如 img/ */
    private String dir;

    /** AccessKey ID */
    private String accessKey;

    /** AccessKey Secret */
    private String secretKey;
}
