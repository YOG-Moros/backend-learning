package top.cch.file.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public record StoredObject(String objectKey, long size, String contentType, InputStream stream)
        implements Closeable {

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
