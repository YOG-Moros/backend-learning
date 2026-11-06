package top.cch.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.cch.file.config.OssConfig;
import top.cch.file.service.OssService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * OSS 上传服务实现
 *
 * <p>流程：校验是不是图片 → 用 UUID 重命名 → 设置 ContentType →
 * 调 SDK 的 putObject 上传 → 拼出访问链接返回。</p>
 *
 * @author cch
 * @date 2026/11/4
 * @description 作业要求 2 + 拓展（多图上传、拦截非图片）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssConfig ossConfig;

    /**
     * 允许上传的图片后缀 -> 对应的 Content-Type。
     * 拓展 2：不在这个白名单里的后缀直接拦截；上传时用它设置 ContentType，
     * 浏览器打开链接才会直接显示图片，而不是变成下载文件。
     */
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".gif", "image/gif",
            ".bmp", "image/bmp",
            ".webp", "image/webp"
    );

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "上传失败：请选择文件";
        }
        // 1. 取原始文件名，截出后缀（统一转小写，.JPG 也放行）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf('.') < 0) {
            return "上传失败：文件缺少图片扩展名";
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // 2. 拓展 2：后缀不在图片白名单里，直接拦截
        if (!IMAGE_TYPES.containsKey(suffix)) {
            log.warn("拦截非图片文件: {}（后缀 {}）", originalFilename, suffix);
            return "上传失败：仅支持图片类型 jpg/jpeg/png/gif/bmp/webp";
        }

        if (!top.cch.file.support.FileTypeDetector.isImageType(top.cch.file.support.FileTypeDetector.detect(file))) {
            return "上传失败：文件内容不是图片";
        }
        if (ossConfig.getEndpoint() == null || ossConfig.getAccessKey() == null || ossConfig.getSecretKey() == null) {
            return "上传失败：请配置 OSS 环境变量或外部 aliyun-oss.properties";
        }

        // 3. 用 UUID 重命名，避免中文/重名文件互相覆盖
        String newFileName = UUID.randomUUID() + suffix;
        String uploadPath = ossConfig.getDir() + newFileName;
        log.info("新文件名: {}", newFileName);

        // 4. 创建 OSS 客户端并上传
        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(), ossConfig.getAccessKey(), ossConfig.getSecretKey());
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            // 不设置的话默认 application/octet-stream，浏览器访问会变成下载
            meta.setContentType(IMAGE_TYPES.get(suffix));
            ossClient.putObject(ossConfig.getBucket(), uploadPath, inputStream, meta);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return "上传失败：" + e.getMessage();
        } finally {
            ossClient.shutdown();
        }

        // 5. 拼访问链接：https://bucket.endpoint/目录/文件名
        String url = "https://" + ossConfig.getBucket() + "." + ossConfig.getEndpoint() + "/" + uploadPath;
        log.info("上传成功，访问链接: {}", url);
        return url;
    }

    @Override
    public List<String> upload(MultipartFile[] files) {
        // 拓展 1：多图上传就是把单图上传循环一遍，逐个返回结果
        List<String> urls = new ArrayList<>();
        if (files == null || files.length == 0) {
            urls.add("上传失败：请选择文件");
            return urls;
        }
        for (MultipartFile file : files) {
            urls.add(upload(file));
        }
        return urls;
    }
}
