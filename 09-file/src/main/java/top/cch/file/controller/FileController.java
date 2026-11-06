package top.cch.file.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import top.cch.file.common.Result;
import top.cch.file.dto.FileUploadResult;
import top.cch.file.service.FileService;
import top.cch.file.storage.StoredObject;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<FileUploadResult> upload(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "dir", required = false) String dir) {
        return Result.ok(fileService.upload(file, dir));
    }

    @PostMapping("/upload/image")
    public Result<FileUploadResult> uploadImage(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "dir", required = false) String dir) {
        return Result.ok(fileService.uploadImage(file, dir));
    }

    @PostMapping("/upload/batch")
    public Result<List<FileUploadResult>> uploadBatch(@RequestParam("files") MultipartFile[] files,
                                                      @RequestParam(value = "dir", required = false) String dir) {
        return Result.ok(fileService.uploadBatch(files, dir));
    }

    @GetMapping("/download/{*key}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String key) {
        return stream(key, true);
    }

    @GetMapping("/preview/{*key}")
    public ResponseEntity<StreamingResponseBody> preview(@PathVariable String key) {
        return stream(key, false);
    }

    @GetMapping("/url/{*key}")
    public Result<Map<String, Object>> url(@PathVariable String key,
                                           @RequestParam(value = "expireMinutes", defaultValue = "30") int expireMinutes) {
        String objectKey = fileService.normalizeKey(key);
        return Result.ok(Map.of(
                "url", fileService.url(objectKey, Duration.ofMinutes(expireMinutes)),
                "expireMinutes", expireMinutes));
    }

    @DeleteMapping("/{*key}")
    public Result<Void> delete(@PathVariable String key) {
        fileService.delete(fileService.normalizeKey(key));
        return Result.ok(null);
    }

    @GetMapping("/storage")
    public Result<Map<String, Object>> storage() {
        return Result.ok(Map.of("storage", fileService.storageType()));
    }

    private ResponseEntity<StreamingResponseBody> stream(String key, boolean attachment) {
        String objectKey = fileService.normalizeKey(key);
        StoredObject object = fileService.read(objectKey);
        String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
        return fileService.stream(object, fileName, attachment);
    }
}
