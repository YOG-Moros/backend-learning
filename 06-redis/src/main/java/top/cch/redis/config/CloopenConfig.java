package top.cch.redis.config;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 容联云短信配置，读取 application.yml 里 cch.sms.ccp.* 并初始化 SDK 客户端
 *
 * @author cch
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cch.sms.ccp")
public class CloopenConfig {

    /** 服务地址，生产环境 app.cloopen.com */
    private String serverIp;

    /** 服务端口，生产环境 8883 */
    private String port;

    /** 主账户 ACCOUNT SID */
    private String accountSid;

    /** 主账户 AUTH TOKEN */
    private String accountToken;

    /** 应用 ID */
    private String appId;

    /** 短信模板 ID，在容联云控制台的短信模板管理里查看 */
    private String templateId;

    @Bean
    public CCPRestSmsSDK ccprRestSmsSDK() {
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(serverIp, port);
        sdk.setAccount(accountSid, accountToken);
        sdk.setAppId(appId);
        // 官方 Java SDK 文档示例要求显式设置为 JSON 报文格式
        sdk.setBodyType(BodyType.Type_JSON);
        return sdk;
    }
}
