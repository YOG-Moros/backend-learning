package top.cch.doc.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("图书管理接口")
                        .version("1.0.0")
                        .description("""
                                这是 11-doc 模块的示例文档。
                                <p>
                                文档由代码注解生成，改代码即改文档，不需要额外维护一份 Markdown。
                                """)
                        .contact(new Contact().name("cch").email("cch@example.com"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://127.0.0.1:8011").description("本地开发"),
                        new Server().url("https://api.example.com").description("生产环境")))
                .externalDocs(new ExternalDocumentation()
                        .description("模块教程")
                        .url("https://github.com/cch/backend-learning"));
    }

    @Bean
    public GroupedOpenApi bookGroup() {
        return GroupedOpenApi.builder()
                .group("book")
                .displayName("图书业务")
                .pathsToMatch("/books/**")
                .build();
    }

    @Bean
    public GroupedOpenApi opsGroup() {
        return GroupedOpenApi.builder()
                .group("ops")
                .displayName("运维接口")
                .pathsToMatch("/ops/**", "/actuator/**")
                .build();
    }
}
