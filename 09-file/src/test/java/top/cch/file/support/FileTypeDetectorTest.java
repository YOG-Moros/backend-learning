package top.cch.file.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileTypeDetectorTest {

    @Test
    void 魔数识别jpeg_png_gif三种图片() {
        assertThat(FileTypeDetector.detect(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10}, 6))
                .isEqualTo("jpeg");
        assertThat(FileTypeDetector.detect(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, 8))
                .isEqualTo("png");
        assertThat(FileTypeDetector.detect("GIF89a".getBytes(), 6)).isEqualTo("gif");
    }

    @Test
    void 魔数识别pdf() {
        assertThat(FileTypeDetector.detect("%PDF-1.7".getBytes(), 9)).isEqualTo("pdf");
    }

    @Test
    void 魔数识别zip() {
        assertThat(FileTypeDetector.detect(new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00}, 6)).isEqualTo("zip");
    }

    @Test
    void 魔数识别exe与elf() {
        assertThat(FileTypeDetector.detect(new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00}, 6)).isEqualTo("exe");
        assertThat(FileTypeDetector.detect(new byte[]{0x7F, 0x45, 0x4C, 0x46, 0x02, 0x01}, 6)).isEqualTo("elf");
    }

    @Test
    void 短文件头与空数据返回unknown() {
        assertThat(FileTypeDetector.detect(new byte[]{(byte) 0xFF, (byte) 0xD8}, 2)).isEqualTo("unknown");
        assertThat(FileTypeDetector.detect(new byte[0], 0)).isEqualTo("unknown");
        assertThat(FileTypeDetector.detect(null, 0)).isEqualTo("unknown");
        assertThat(FileTypeDetector.detect(new byte[]{0x00, 0x00, 0x00, 0x00}, 4)).isEqualTo("unknown");
    }

    @Test
    void isImageExtension判定图片扩展名() {
        assertThat(FileTypeDetector.isImageExtension("jpg")).isTrue();
        assertThat(FileTypeDetector.isImageExtension("jpeg")).isTrue();
        assertThat(FileTypeDetector.isImageExtension("png")).isTrue();
        assertThat(FileTypeDetector.isImageExtension("gif")).isTrue();
        assertThat(FileTypeDetector.isImageExtension("webp")).isTrue();
        assertThat(FileTypeDetector.isImageExtension("bmp")).isTrue();
        assertThat(FileTypeDetector.isImageExtension("txt")).isFalse();
        assertThat(FileTypeDetector.isImageExtension("pdf")).isFalse();
        assertThat(FileTypeDetector.isImageExtension("zip")).isFalse();
        assertThat(FileTypeDetector.isImageExtension("")).isFalse();
    }

    @Test
    void isImageType与isExecutable判定内容类型() {
        assertThat(FileTypeDetector.isImageType("jpeg")).isTrue();
        assertThat(FileTypeDetector.isImageType("png")).isTrue();
        assertThat(FileTypeDetector.isImageType("gif")).isTrue();
        assertThat(FileTypeDetector.isImageType("zip")).isFalse();
        assertThat(FileTypeDetector.isImageType("unknown")).isFalse();
        assertThat(FileTypeDetector.isExecutable("exe")).isTrue();
        assertThat(FileTypeDetector.isExecutable("elf")).isTrue();
        assertThat(FileTypeDetector.isExecutable("png")).isFalse();
    }
}
