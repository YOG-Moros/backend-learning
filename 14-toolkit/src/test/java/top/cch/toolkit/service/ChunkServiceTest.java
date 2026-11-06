package top.cch.toolkit.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import top.cch.toolkit.config.ToolkitProperties;
import top.cch.toolkit.dto.ChunkInitRequest;
import top.cch.toolkit.common.BusinessException;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;

class ChunkServiceTest {
    @TempDir Path directory;
    private ChunkService service;
    @BeforeEach void setup() {
        var properties=new ToolkitProperties(); properties.setWorkDir(directory.toString());
        service=new ChunkService(properties); service.init();
    }
    @AfterEach void cleanup() { service.destroy(); }
    private MockMultipartFile part(String text) { return new MockMultipartFile("file","part.txt","text/plain",text.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
    @Test void outOfOrderUploadCanResumeAndMergeInOrder() throws Exception {
        var session=service.init(new ChunkInitRequest("test.txt",2,null));
        service.upload(session.uploadId(),1,part("world"));
        var resumed=service.init(new ChunkInitRequest("test.txt",2,null,session.uploadId()));
        assertThat(resumed.uploaded()).containsExactly(1);
        service.upload(session.uploadId(),0,part("hello "));
        var merged=service.merge(session.uploadId());
        assertThat(Files.readString(service.file(merged.fileId()).toPath())).isEqualTo("hello world");
        assertThatThrownBy(()->service.merge(session.uploadId())).isInstanceOf(BusinessException.class);
    }
    @Test void missingChunkAndWrongDigestDoNotPublishCorruptFiles() throws Exception {
        var session=service.init(new ChunkInitRequest("test.txt",2,"00000000000000000000000000000000"));
        service.upload(session.uploadId(),0,part("hello"));
        assertThatThrownBy(()->service.merge(session.uploadId())).isInstanceOf(BusinessException.class);
        service.upload(session.uploadId(),1,part("world"));
        assertThatThrownBy(()->service.merge(session.uploadId())).isInstanceOf(BusinessException.class);
        try(var files=Files.list(directory.resolve("uploads"))) { assertThat(files).isEmpty(); }
    }
    @Test void retryReplacesOnlyItsOwnPartAndBadIndexesAreRejected() {
        var session=service.init(new ChunkInitRequest("test.txt",2,null));
        service.upload(session.uploadId(),0,part("old"));
        service.upload(session.uploadId(),0,part("new"));
        assertThat(service.status(session.uploadId()).uploaded()).containsExactly(0);
        assertThatThrownBy(()->service.upload(session.uploadId(),2,part("bad"))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(()->service.init(new ChunkInitRequest("different.txt",2,null,session.uploadId()))).isInstanceOf(BusinessException.class);
    }
}
