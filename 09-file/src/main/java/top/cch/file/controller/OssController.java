package top.cch.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.cch.file.service.OssService;

import java.util.List;

/**
 * OSS 上传接口
 *
 * @author cch
 * @date 2026/11/4
 * @description 作业要求 3：接口测试验证
 */
@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    /**
     * 单图上传：POST /oss/upload，form-data 里参数名 file
     */
    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        return ossService.upload(file);
    }

    /**
     * 多图上传：POST /oss/uploads，form-data 里多个参数名都是 files
     */
    @PostMapping("/uploads")
    public List<String> upload(MultipartFile[] files) {
        return ossService.upload(files);
    }
}
