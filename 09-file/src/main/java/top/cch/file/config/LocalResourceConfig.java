package top.cch.file.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class LocalResourceConfig implements WebMvcConfigurer {

    private final FileProperties properties;

    public LocalResourceConfig(FileProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPrefix = properties.getLocal().getUrlPrefix();
        Path base = Paths.get(properties.getLocal().getBaseDir()).toAbsolutePath().normalize();
        String location = base.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(location);
    }
}
