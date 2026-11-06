package top.cch.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OSS 文件上传服务
 *
 * @author cch
 * @date 2026/11/4
 * @description 作业要求 2：图片上传；拓展 1：多图上传
 */
public interface OssService {

    /**
     * 单图上传
     *
     * @param file 前端提交的文件
     * @return 成功返回图片访问链接，失败返回失败原因
     */
    String upload(MultipartFile file);

    /**
     * 多图上传
     *
     * @param files 前端提交的文件数组
     * @return 每个文件对应一条结果（链接或失败原因），顺序与入参一致
     */
    List<String> upload(MultipartFile[] files);
}
