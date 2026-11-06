package top.cch.file.dto;

public record FileUploadResult(String objectKey, String originalName, String url, long size, String contentType,
                               String storage) {
}
