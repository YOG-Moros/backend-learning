package top.cch.file.support;

import org.springframework.web.multipart.MultipartFile;
import top.cch.file.common.BusinessException;
import top.cch.file.common.ErrorCode;

import java.io.IOException;
import java.io.InputStream;

public final class FileTypeDetector {

    private static final int HEAD_LENGTH = 12;

    private FileTypeDetector() {
    }

    public static String detect(MultipartFile file) {
        byte[] head = new byte[HEAD_LENGTH];
        int length;
        try (InputStream in = file.getInputStream()) {
            length = in.readNBytes(head, 0, HEAD_LENGTH);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR, "读取文件头失败");
        }
        return detect(head, length);
    }

    public static String detect(byte[] head, int length) {
        if (head == null || length <= 0) {
            return "unknown";
        }
        if (startsWith(head, length, 0xFF, 0xD8, 0xFF)) {
            return "jpeg";
        }
        if (startsWith(head, length, 0x89, 0x50, 0x4E, 0x47)) {
            return "png";
        }
        if (startsWith(head, length, 0x47, 0x49, 0x46, 0x38)) {
            return "gif";
        }
        if (startsWith(head, length, 0x42, 0x4D)) return "bmp";
        if (length >= 12 && startsWith(head, length, 0x52, 0x49, 0x46, 0x46)
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') return "webp";
        if (startsWith(head, length, 0x25, 0x50, 0x44, 0x46)) {
            return "pdf";
        }
        if (startsWith(head, length, 0x50, 0x4B, 0x03, 0x04)) {
            return "zip";
        }
        if (startsWith(head, length, 0x4D, 0x5A)) {
            return "exe";
        }
        if (startsWith(head, length, 0x7F, 0x45, 0x4C, 0x46)) {
            return "elf";
        }
        return "unknown";
    }

    public static boolean isExecutable(String type) {
        return "exe".equals(type) || "elf".equals(type);
    }

    public static boolean isImageExtension(String extension) {
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp", "bmp" -> true;
            default -> false;
        };
    }

    public static boolean isImageType(String type) {
        return isImageExtension(type);
    }

    public static boolean matchesImage(String extension, String type) {
        return ("jpg".equals(extension) ? "jpeg" : extension).equals(type);
    }

    private static boolean startsWith(byte[] head, int length, int... magic) {
        if (length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if ((head[i] & 0xFF) != magic[i]) {
                return false;
            }
        }
        return true;
    }
}
